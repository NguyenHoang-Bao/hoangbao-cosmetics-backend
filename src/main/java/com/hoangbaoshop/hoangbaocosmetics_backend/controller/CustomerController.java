package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.customer.CustomerLookupResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * Tra cứu thông tin khách hàng và điểm tích lũy theo số điện thoại
     * Phục vụ thống nhất khách hàng giữa Online và POS Bán lẻ tại quầy
     */
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<CustomerLookupResponse>> lookupCustomer(@RequestParam String phone) {
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng cung cấp số điện thoại khách hàng!"));
        }

        String cleanPhone = phone.trim().replaceAll("\\s+", "");
        Optional<User> userOpt = userRepository.findByPhoneNumber(cleanPhone);

        long orderCount = orderRepository.countByPhoneNumber(cleanPhone);
        Double totalSpent = orderRepository.sumTotalAmountByPhone(cleanPhone);
        if (totalSpent == null) totalSpent = 0.0;

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int points = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
            String tier = calculateTier(points, totalSpent);

            CustomerLookupResponse response = CustomerLookupResponse.builder()
                    .idUser(user.getIdUser())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .loyaltyPoints(points)
                    .membershipTier(tier)
                    .totalOrders(orderCount)
                    .totalSpent(totalSpent)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response, "Tra cứu khách hàng thành công!"));
        } else {
            // Trường hợp khách chưa đăng ký tài khoản nhưng đã từng mua hàng qua SĐT này
            if (orderCount > 0) {
                int estimatedPoints = (int) (totalSpent / 20000);
                CustomerLookupResponse response = CustomerLookupResponse.builder()
                        .fullName("Khách thân thiết")
                        .phoneNumber(cleanPhone)
                        .loyaltyPoints(estimatedPoints)
                        .membershipTier(calculateTier(estimatedPoints, totalSpent))
                        .totalOrders(orderCount)
                        .totalSpent(totalSpent)
                        .build();

                return ResponseEntity.ok(ApiResponse.success(response, "Khách hàng có lịch sử mua hàng!"));
            }

            return ResponseEntity.ok(ApiResponse.success(null, "Khách hàng mới chưa có trên hệ thống"));
        }
    }

    private String calculateTier(int points, double totalSpent) {
        if (points >= 500 || totalSpent >= 10000000) {
            return "KIM CƯƠNG";
        } else if (points >= 200 || totalSpent >= 4000000) {
            return "VÀNG";
        } else if (points >= 50 || totalSpent >= 1000000) {
            return "BẠC";
        }
        return "THÀNH VIÊN MỚI";
    }
}
