# BÀI HỌC BACKEND 12: FORWARDED HEADERS, SPRING BOOT ACTUATOR & SCHEDULED CLEANUP CRON JOB

> **Dự án**: Hoang Bao Cosmetics E-Commerce Backend  
> **Tài liệu dành cho**: Ôn tập kiến trúc, chuẩn bị triển khai Cloud (Render/Railway/VPS) và cẩm nang trả lời phỏng vấn Intern/Fresher.

---

## 1. TỔNG QUAN 3 TÍNH NĂNG ĐÃ NÂNG CẤP

Trong nhánh `feature/production-hardening-and-scheduler`, hệ thống Backend đã được trang bị 3 vũ khí kỹ thuật chuẩn doanh nghiệp:
1. **Reverse Proxy & Forwarded Headers**: Nhận diện chính xác IP thật và giao thức HTTPS từ Cloud/Nginx, chống lỗi chặn nhầm Rate Limit.
2. **Spring Boot Actuator (`/actuator/health`)**: Cung cấp endpoint giám sát trạng thái hệ thống, phục vụ Zero-Downtime Deployment và tự động phục hồi (Auto-restart) trên Docker/Render/Railway.
3. **Scheduled Cleanup Task (`@Scheduled`)**: Tự động dọn dẹp các mã OTP hết hạn và token đã sử dụng vào 2h sáng hàng ngày, chống phình to cơ sở dữ liệu.

---

## 2. BÀI HỌC 1: FORWARDED HEADERS & REVERSE PROXY

### 2.1. Vấn đề thực tế khi Deploy lên Cloud
* Khi chạy ở máy tính cá nhân (Localhost): Trình duyệt gửi trực tiếp request tới cổng 8080 của Spring Boot. Hàm `request.getRemoteAddr()` luôn trả về đúng IP của máy (`127.0.0.1`).
* Khi deploy lên các nền tảng Cloud (Render, Railway, AWS ALB, Nginx):
  $$\text{Client (IP Thật)} \longrightarrow \mathbf{[\text{Render Reverse Proxy / Nginx}]} \longrightarrow \text{Spring Boot Container}$$
* Vì Render đứng ở giữa làm nhiệm vụ cấp SSL HTTPS, nó forward request nội bộ sang Spring Boot. Nếu không cấu hình, Spring Boot chỉ nhìn thấy **IP của chính con Reverse Proxy** (ví dụ: `10.0.0.1`).
* **Hậu quả nghiêm trọng**: `RateLimitingFilter` của ta giới hạn 30 request/phút theo IP. Khi đó, **toàn bộ người dùng trên Internet vào web đều bị tính chung vào 1 IP duy nhất (`10.0.0.1`)**. Chỉ cần 1 người click nhanh, toàn bộ khách truy cập khác (kể cả nhà tuyển dụng đang test) đều bị văng lỗi `429 Too Many Requests`!

### 2.2. Giải pháp kỹ thuật
Thêm vào `application.yml`:
```yaml
server:
  forward-headers-strategy: framework
```
* **Cách hoạt động**: Spring Boot sẽ tự động wrap `HttpServletRequest`, đọc các header tiêu chuẩn của proxy:
  * `X-Forwarded-For`: Lấy đúng IP thật của người dùng cuối.
  * `X-Forwarded-Proto`: Nhận biết request gốc là `https` chứ không phải `http`, tránh lỗi Mixed Content hoặc redirect sai giao thức.

### 2.3. Câu hỏi phỏng vấn kinh điển
* **Câu hỏi**: *"Khi deploy Spring Boot phía sau Nginx hoặc Load Balancer, làm thế nào để lấy được IP thật của Client và xử lý HTTPS redirect?"*
* **Cách trả lời ăn điểm**:
  > *"Dạ, khi đứng sau Reverse Proxy, `request.getRemoteAddr()` sẽ trả về IP nội bộ của proxy. Em giải quyết bằng cách cấu hình `server.forward-headers-strategy: framework` trong `application.yml` để Spring Boot tự động phân giải các header `X-Forwarded-For` và `X-Forwarded-Proto`. Nhờ vậy, bộ lọc Rate Limiting và Audit Log của em luôn ghi nhận chính xác IP thực của từng khách hàng."*

---

## 3. BÀI HỌC 2: SPRING BOOT ACTUATOR & HEALTH CHECK

### 3.1. Vấn đề thực tế
* Quá trình Spring Boot khởi động (kết nối DB, nạp Hibernate, dựng context) mất từ 15-30 giây.
* Nếu Cloud không biết khi nào app đã sẵn sàng nhận traffic, nó có thể mở cổng sớm gây lỗi `502 Bad Gateway` cho người dùng.
* Nếu ứng dụng bị rò rỉ bộ nhớ (OOM) hoặc deadlock database, server bị đơ (chết lâm sàng) nhưng tiến trình Java chưa tắt, Cloud không biết để tự restart.

### 3.2. Giải pháp kỹ thuật
1. **Khai báo thư viện trong `pom.xml`**:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```
2. **Cấu hình trong `application.yml`**:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info
     endpoint:
       health:
         show-details: when_authorized
   ```
3. **Mở quyền trong `SecurityConfig.java`**:
   ```java
   .requestMatchers("/actuator/health", "/actuator/info").permitAll()
   ```

### 3.3. Sử dụng khi Deploy (Render / Railway / Kubernetes)
* Trên Render/Railway, tìm mục **Health Check Path** và điền: `/actuator/health`.
* Nền tảng sẽ liên tục thăm dò endpoint này. Khi nhận được HTTP 200 `{"status": "UP"}`, nền tảng mới mở kết nối cho khách hàng truy cập.

### 3.4. Câu hỏi phỏng vấn kinh điển
* **Câu hỏi**: *"Làm sao em biết ứng dụng Spring Boot của em trên server đang sống hay chết để thiết lập auto-scaling hoặc auto-restart?"*
* **Cách trả lời ăn điểm**:
  > *"Em tích hợp `spring-boot-starter-actuator` và public endpoint `/actuator/health`. Endpoint này tự động kiểm tra tính sẵn sàng của JVM, kết nối MySQL và dung lượng đĩa. Em gắn endpoint này vào Liveness và Readiness Probe của Docker/Cloud để hệ thống tự động kiểm tra sức khỏe và auto-restart khi gặp sự cố."*

---

## 4. BÀI HỌC 3: SCHEDULED TASKS & CRON JOB DỌN RÁC DATABASE

### 4.1. Vấn đề thực tế
* Bảng `otp_tokens` lưu mã xác thực tài khoản và mã quên mật khẩu. Token chỉ có hạn 5 phút hoặc 24 giờ.
* Sau vài tháng hoạt động, hàng chục nghìn bản ghi OTP rác tích tụ trong bảng làm database phình to và làm chậm tốc độ tìm kiếm.

### 4.2. Giải pháp kiến trúc chuẩn SOLID
1. **Tầng Repository (`OtpTokenRepository.java`)**:
   Dùng `@Modifying` + `@Transactional` để thực hiện xóa hàng loạt (Bulk Delete) tối ưu tốc độ trực tiếp ở cấp Database:
   ```java
   @Modifying
   @Transactional
   @Query("DELETE FROM OtpToken o WHERE o.expiryTime < :cutoffTime OR o.isUsed = true")
   int deleteExpiredOrUsedTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
   ```
2. **Bật cơ chế lập lịch (`AsyncConfig.java`)**:
   ```java
   @Configuration
   @EnableAsync
   @EnableScheduling
   public class AsyncConfig { ... }
   ```
3. **Tầng Scheduler (`OtpCleanupScheduler.java`)**:
   ```java
   @Component
   @RequiredArgsConstructor
   @Slf4j
   public class OtpCleanupScheduler {

       private final OtpTokenRepository otpTokenRepository;

       @Scheduled(cron = "0 0 2 * * ?") // 02:00:00 sáng mỗi ngày
       public void cleanupExpiredOtpTokens() {
           LocalDateTime now = LocalDateTime.now();
           int deletedCount = otpTokenRepository.deleteExpiredOrUsedTokens(now);
           if (deletedCount > 0) {
               log.info("[CronJob] Đã dọn dẹp {} mã OTP rác khỏi Database.", deletedCount);
           }
       }
   }
   ```

### 4.3. Cú pháp Cron Expression trong Spring Boot:
$$\mathbf{0 \quad 0 \quad 2 \quad * \quad * \quad ?}$$
* `0`: Giây thứ 0
* `0`: Phút thứ 0
* `2`: 2 giờ sáng
* `*`: Mọi ngày trong tháng
* `*`: Mọi tháng
* `?`: Không quan tâm thứ trong tuần

### 4.4. Câu hỏi phỏng vấn kinh điển
* **Câu hỏi**: *"Hệ thống có nhiều dữ liệu tạm hết hạn (như OTP, giỏ hàng bỏ quên, token xác thực), em xử lý dọn dẹp như thế nào?"*
* **Cách trả lời ăn điểm**:
  > *"Em sử dụng `@EnableScheduling` và xây dựng class Scheduler riêng biệt với cron job chạy vào giờ thấp điểm (2h sáng). Tại Repository, em viết câu lệnh `@Modifying @Query` để xóa bulk trực tiếp trên Database nhằm tránh việc phải load từng entity lên bộ nhớ gây tốn RAM. Em cũng viết Unit Test giả lập đầy đủ cho Scheduler bằng Mockito."*

---

## 5. TỔNG KẾT BẢNG KIỂM TRA (CHECKLIST TRƯỚC KHI DEPLOY)
- [x] Forwarded Headers: Đã cấu hình `server.forward-headers-strategy: framework`.
- [x] Spring Boot Actuator: Đã cấu hình `/actuator/health` và mở quyền trong Security.
- [x] Scheduled Task: Đã kích hoạt cron job 2h sáng dọn rác OTP, kèm test 100% pass.
- [x] Test suite: Đạt 80/80 unit tests thành công.
