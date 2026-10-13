# BÀI HỌC BACKEND 13: KHẮC PHỤC TRIỆT ĐỂ LỖI P0 WEBHOOK THANH TOÁN, CHỐNG VÉT KHO & BẢO VỆ HẠ TẦNG DEMO

> **Dự án**: Hoang Bao Cosmetics E-Commerce Backend  
> **Nhánh**: `feature/production-hardening-and-scheduler`  
> **Mục tiêu**: Chuẩn hóa hệ sinh thái thanh toán SePay, chống DoS cạn kho ảo, bảo mật Rate Limiting chống spoofing IP và tối ưu hóa hạ tầng Docker/AWS.

---

## 1. TỔNG QUAN CÁC NÂNG CẤP ĐÃ THỰC THI (9 LỖI TINH GỌN)

Theo đúng định hướng tinh gọn cho phiên bản Demo/MVP, toàn bộ 9 lỗi trọng yếu đã được xử lý triệt để mà không cần xáo trộn cấu trúc Database:

| Nhóm | Bug ID | Vấn đề ban đầu | Giải pháp kỹ thuật đã triển khai | File thay đổi |
| :--- | :--- | :--- | :--- | :--- |
| **P0: Bảo mật** | **BUG-03** | Khách vượt Rate Limit bằng `X-Forwarded-For` giả | Chuyển `server.forward-headers-strategy: native`, `getClientIp()` trả về `request.getRemoteAddr()` | `application.yml`, `RateLimitingFilter.java` |
| **P1: Webhook** | **BUG-08** | Webhook trả 400 cho giao dịch lạ gây vòng lặp retry | Trả HTTP 200 cho giao dịch lạ/không có mã đơn; chỉ trả 401 khi sai API Key | `PaymentServiceImpl.java`, `PaymentController.java` |
| **P0: Thanh toán** | **BUG-01** | Regex không bắt được mã đơn thật | Mã đơn: `HB + yyyyMMdd + XXXXXX` (không dấu `-`). Chuẩn hóa xóa ký tự đặc biệt trước khi regex | `OrderServiceImpl.java`, `PaymentServiceImpl.java`, test files |
| **P1: Thanh toán** | **BUG-04** | Chuyển tiền lần 2 vào đơn đã xử lý bị bỏ qua im lặng | Ghi thêm ghi chú `[CHUYỂN TRÙNG - CẦN HOÀN]` kèm số tiền và txId vào note đơn hàng | `PaymentServiceImpl.java` |
| **P0: Đặt hàng** | **BUG-02** | Gom hết kho, spam đơn và spam email | 1. `@Max(10)` cho số lượng, `@Size(max = 20)` cho items.<br>2. Rate limit 5 đơn/phút cho `POST /api/orders`.<br>3. `ExpiredOrderScheduler` tự động hủy đơn và hoàn kho sau 30 phút | `OrderItemRequest.java`, `CreateOrderRequest.java`, `RateLimitingFilter.java`, `OrderRepository.java`, `OrderService.java`, `OrderServiceImpl.java`, `ExpiredOrderScheduler.java` |
| **P2: Vận hành** | **BUG-10** | Lệch múi giờ 7 tiếng | Thiết lập `Asia/Ho_Chi_Minh` ở `@PostConstruct` Application, Dockerfile và `@Scheduled` | `HoangbaocosmeticsBackendApplication.java`, `OtpCleanupScheduler.java`, `Dockerfile` |
| **P2: Cấu hình** | **BUG-11** | `ddl-auto: update` & Swagger lộ trên Production | Tạo `application-prod.yml` với `ddl-auto: validate` và tắt `springdoc` | `src/main/resources/application-prod.yml` |
| **P2: Hạ tầng** | **BUG-13** | OOM Killer trên máy chủ RAM 1GB | Dockerfile JVM `-XX:MaxRAMPercentage=70.0 -XX:+UseG1GC`, hướng dẫn Swap 2GB & MySQL buffer pool | `Dockerfile` |

---

## 2. CHI TIẾT KỸ THUẬT TỪNG BƯỚC THỰC HIỆN

### 2.1. BUG-03: Rate Limit Chống Giả Mạo Header `X-Forwarded-For`
* **Nguyên nhân**: Code cũ đọc phần tử đầu tiên của `X-Forwarded-For` do client gửi lên. Kẻ tấn công chỉ cần random một IP giả trong header là qua mặt toàn bộ rate limiting.
* **Khắc phục**:
  1. Trong `application.yml`:
     ```yaml
     server:
       forward-headers-strategy: native
     ```
  2. Trong `RateLimitingFilter.java`:
     ```java
     private String getClientIp(HttpServletRequest request) {
         return request.getRemoteAddr();
     }
     ```
  3. Khi đứng sau Nginx, Tomcat `RemoteIpValve` (kích hoạt bởi mode `native`) sẽ phân giải IP an toàn từ proxy tin cậy.

### 2.2. BUG-08: Webhook SePay Không Bị Retry Vô Tận Khi Nhận Giao Dịch Lạ
* **Nguyên nhân**: Khi tài khoản ngân hàng nhận tiền chuyển khoản cá nhân hoặc không có mã đơn hàng, code trả về `false` khiến controller trả về HTTP 400. SePay coi đây là lỗi mạng và gửi lại liên tục.
* **Khắc phục**:
  1. Trong `PaymentServiceImpl.java`: Nếu nội dung trống, không tìm thấy mã đơn, hoặc không thấy đơn hàng trong DB $\rightarrow$ ghi log cảnh báo và `return true`.
  2. Trong `PaymentController.java`: Chỉ trả về `HttpStatus.UNAUTHORIZED` (401) khi sai API Key xác thực.

### 2.3. BUG-01: Chuẩn Hóa Mã Đơn Hàng & Regex Thông Minh
* **Nguyên nhân**: 
  - Mã đơn cũ: `ORD-20261013-A1B2C3` (có 2 dấu `-`).
  - Regex cũ: `ORD-[A-Za-z0-9]+` dừng lại ở dấu `-` thứ hai và trích xuất nhầm `ORD-20261013`.
  - App ngân hàng khi chuyển khoản thường tự xóa dấu gạch nối khiến regex cũ không bắt được gì.
* **Khắc phục**:
  1. `OrderServiceImpl.java`: Sinh mã đơn dạng chữ hoa liền mạch `HB20261013A1B2C3`:
     ```java
     private String generateOrderCode() {
         String datePart = LocalDate.now().format(ORDER_DATE_FORMATTER);
         String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
         return "HB" + datePart + randomPart;
     }
     ```
  2. `PaymentServiceImpl.java`: Chuẩn hóa nội dung chuyển khoản trước khi khớp mẫu:
     ```java
     private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("HB\\d{8}[A-Z0-9]{6}");

     String normalized = content.toUpperCase().replaceAll("[^A-Z0-9]", "");
     Matcher matcher = ORDER_CODE_PATTERN.matcher(normalized);
     String orderCode = matcher.find() ? matcher.group() : null;
     ```
     Dù khách chuyển với nội dung `CK hb-20261013-a1b2c3` hay `Thanh toan HB20261013A1B2C3`, hệ thống đều trích xuất chính xác 100%.

### 2.4. BUG-04: Lưu Vết Giao Dịch Chuyển Trùng (Thay Bước 8)
* **Khắc phục**:
  ```java
  if (order.getStatus() != OrderStatus.PENDING) {
      log.info("SePay Webhook: Đơn hàng {} đã ở trạng thái {} trước đó.", orderCode, order.getStatus());
      String note = order.getNote() != null ? order.getNote() : "";
      if (!note.contains(txId)) {
          order.setNote(note + String.format(" | [CHUYỂN TRÙNG - CẦN HOÀN]: %,.0f VNĐ (Mã GD: %s)",
                  transferAmount.doubleValue(), txId));
          orderRepository.saveAndFlush(order);
      }
      return true;
  }
  ```
  Kế toán có thể tra cứu ngay trên giao diện xem khách nào chuyển trùng tiền, số tiền bao nhiêu và mã giao dịch ngân hàng là gì để đối soát hoàn tiền tay.

### 2.5. BUG-02: Chống Vét Cạn Kho Ảo & Tự Động Hủy Đơn Treo
* **Khắc phục**:
  1. **Validation Bean**:
     - `OrderItemRequest`: `@Max(value = 10, message = "Số lượng mỗi sản phẩm tối đa là 10")`
     - `CreateOrderRequest`: `@Size(max = 20, message = "Đơn hàng không được vượt quá 20 loại sản phẩm")`
  2. **Rate Limit Đặt Hàng**:
     - Trong `RateLimitingFilter.java`: Giới hạn `POST /api/orders` tối đa 5 đơn/phút mỗi IP.
  3. **Tự động hủy đơn chuyển khoản quá hạn**:
     - `OrderRepository.java`: Khai báo `findByStatusAndPaymentMethodInAndCreatedAtBefore(...)`.
     - `OrderServiceImpl.java`: Triển khai `cancelExpiredBankTransferOrders()`: tìm các đơn `PENDING` quá 30 phút, gọi `rollbackStockForOrder()` để trả lại số lượng tồn kho nguyên tử vào Database và đổi trạng thái sang `CANCELLED`.
     - `ExpiredOrderScheduler.java`: Chạy định kỳ mỗi 5 phút (`fixedDelay = 300_000`).

### 2.6. BUG-10, 11, 13: Múi Giờ, Bảo Mật Prod & Tối Ưu RAM
* **Múi giờ Việt Nam (BUG-10)**:
  - Cấu hình `@PostConstruct` trong `HoangbaocosmeticsBackendApplication.java`:
    ```java
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    ```
  - Cấu hình `zone = "Asia/Ho_Chi_Minh"` cho `OtpCleanupScheduler.java`.
* **Cấu hình Production an toàn (BUG-11)**:
  - Tạo `application-prod.yml`:
    ```yaml
    spring:
      jpa:
        hibernate:
          ddl-auto: validate
    springdoc:
      api-docs:
        enabled: false
      swagger-ui:
        enabled: false
    ```
* **Tối ưu RAM 1GB (BUG-13)**:
  - Tạo `Dockerfile` với runtime image Temurin JRE 17 Alpine, set `ENV TZ=Asia/Ho_Chi_Minh`.
  - Tham số khởi chạy JVM: `-XX:MaxRAMPercentage=70.0 -XX:+UseG1GC -Duser.timezone=Asia/Ho_Chi_Minh`.
  - Hướng dẫn tạo Swap 2GB trên VPS Linux:
    ```bash
    sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
    sudo mkswap /swapfile && sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    ```

---

## 3. KẾT QUẢ KIỂM THỬ (TEST RESULTS)

Đã chạy toàn bộ test suite dự án bằng Maven Wrapper:
```bash
./mvnw test
```
* **Tổng số test**: 84 tests
* **Failures**: 0
* **Errors**: 0
* **Skipped**: 1 (Integration test context)
* **Kết quả**: **BUILD SUCCESS 100%**.
