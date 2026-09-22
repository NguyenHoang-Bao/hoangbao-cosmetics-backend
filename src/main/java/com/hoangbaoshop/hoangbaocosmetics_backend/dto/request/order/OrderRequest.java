package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private Integer idUser; // Null nếu khách vãng lai mua tại quầy

    private Integer idStaff; // Nhân viên tạo đơn (nếu mua offline)

    @NotNull(message = "Loại đơn hàng không được để trống")
    private OrderType orderType;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String deliveryAddress;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @Builder.Default
    private Double shippingFee = 0.0;

    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderDetailRequest> items;
}
