# 🌸 HOÀNG BẢO COSMETICS - ENTERPRISE E-COMMERCE BACKEND

> **Hệ thống Backend Bán lẻ & Thương mại Điện tử Mỹ phẩm Đa kênh (Omnichannel: Web Storefront + POS Quầy)**  
> Được thiết kế và xây dựng theo chuẩn kiến trúc doanh nghiệp với Spring Boot 3, Java 17, và MySQL.

---

## 📌 MỤC LỤC
1. [Giới Thiệu Tổng Quan](#-giới-thiệu-tổng-quan)
2. [Ngăn Xếp Công Nghệ (Tech Stack)](#-ngăn-xếp-công-nghệ-tech-stack)
3. [Kiến Trúc Hệ Thống (System Architecture)](#-kiến-trúc-hệ-thống-system-architecture)
4. [Các Giải Pháp Kỹ Thuật Trọng Điểm (Core Engineering Highlights)](#-các-giải-pháp-kỹ-thuật-trọng-điểm-core-engineering-highlights)
5. [Cấu Trúc Thư Mục (Project Structure)](#-cấu-trúc-thư-mục-project-structure)
6. [Tài Liệu API Endpoints](#-tài-liệu-api-endpoints)
7. [Bảo Mật & Phân Quyền (Security & Access Control)](#-bảo-mật--phân-quyền-security--access-control)
8. [Chiến Lược Kiểm Thử (Unit Testing Suite)](#-chiến-lược-kiểm-thử-unit-testing-suite)
9. [Hướng Dẫn Cài Đặt & Triển Khai (Setup & Deployment)](#-hướng-dẫn-cài-đặt--triển-khai-setup--deployment)

---

## 🌟 GIỚI THIỆU TỔNG QUAN

**Hoàng Bảo Cosmetics Backend** là nền tảng máy chủ cung ứng giải pháp quản trị bán lẻ toàn diện cho thương hiệu mỹ phẩm Hoàng Bảo:
- **Khách hàng trực tuyến (E-Commerce Storefront)**: Tìm kiếm sản phẩm, lọc theo danh mục / thương hiệu / tầm giá, quản lý giỏ hàng, đặt hàng giao tận nơi, thanh toán tự động qua mã QR ngân hàng (SePay), đánh giá sản phẩm sau nhận hàng, lưu sản phẩm yêu thích.
- **Bán hàng tại quầy (POS - Point of Sale)**: Tạo đơn bán lẻ siêu tốc tại quầy vật lý, thanh toán tiền mặt/chuyển khoản, tự động trừ tồn kho tức thì và xuất biên lai.
- **Quản trị vận hành & Kho bãi (Admin & Warehouse Management)**: Quản lý biến thể sản phẩm đa thuộc tính (màu sắc, dung tích, SKU), nhập hàng từ nhà cung cấp theo phiếu nhập (`ImportReceipt`), lưu vết sổ cái biến động kho (`InventoryLog`), thống kê doanh thu và báo cáo phân tích thời gian thực.

---

## 🛠 NGĂN XẾP CÔNG NGHỆ (TECH STACK)

| Thành phần | Công nghệ / Thư viện | Phiên bản | Ghi chú |
| :--- | :--- | :--- | :--- |
| **Ngôn ngữ** | Java | 17 LTS | Clean Code, Record, Pattern Matching |
| **Framework** | Spring Boot | 3.4.x | Web, Security, Data JPA, Validation |
| **Bảo mật** | Spring Security & JJWT | 0.11.5 | Stateless JWT Authentication, RBAC |
| **Cơ sở dữ liệu**| MySQL | 8.0+ | InnoDB Engine, ACID Transactions |
| **ORM / Data** | Hibernate / Spring Data JPA | 6.x | Pessimistic Locking, EntityGraph |
| **Công cụ Build**| Apache Maven Wrapper | 3.9+ | Đóng gói JAR, Quản lý phụ thuộc |
| **Tiện ích Code** | Lombok | 1.18+ | Builder Pattern, Slf4j Logging |
| **Tài liệu API** | Springdoc OpenAPI (Swagger UI) | 2.8+ | Tự động sinh tài liệu chuẩn RESTful |
| **Kiểm thử** | JUnit 5 & Mockito | 5.x | 100% Mockito Unit Tests độc lập DB |

---

## 🏛 KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Hệ thống tuân thủ nghiêm ngặt mô hình **Phân lớp Chuẩn Doanh Nghiệp (Strict Layered Architecture)**:

```text
[ Client Layer ] (React 19 Web App / Retail POS Desktop)
       │
       ▼  (HTTPS / RESTful JSON / Bearer Token)
[ Security & Filter Layer ]
  ├── RateLimitingFilter (Chống Brute-force / DDoS HTTP 429)
  ├── CorsFilter (Cấu hình nguồn hợp lệ)
  └── JwtAuthenticationFilter (Giải mã & xác thực JWT Stateless)
       │
       ▼
[ Controller Layer ] (REST API Endpoints, Validation Jakarta @Valid)
       │  (Truyền DTOs - Tuyệt đối không để lộ JPA Entity)
       ▼
[ Service Layer ] (Business Logic, Core Algorithms, @Transactional)
  ├── Quản lý đơn hàng (Online & POS): Khử trùng lặp qua buildOrderLines & writeSaleLedger
  ├── Quản lý kho: Khóa bi quan PESSIMISTIC_WRITE chống âm kho tuyệt đối
  └── Tích hợp Webhook SePay: Idempotent Transaction Lock chống double-spending
       │
       ▼
[ Repository Layer ] (Spring Data JPA, Native SQL, EntityGraph tối ưu N+1)
       │
       ▼
[ Database Layer ] (MySQL 8 - InnoDB, Unique Constraints, Foreign Keys)
```

---

## 💎 CÁC GIẢI PHÁP KỸ THUẬT TRỌNG ĐIỂM (CORE ENGINEERING HIGHLIGHTS)

### 1. Cơ Chế Chống Âm Kho Tuyệt Đối (Pessimistic Locking & Atomic Inventory)
- **Vấn đề**: Khi nhiều khách hàng hoặc thu ngân cùng đặt mua một sản phẩm hot vào cùng một phần nghìn giây, hiện tượng *Race Condition* có thể khiến kho bị bán âm.
- **Giải pháp**:
  - Áp dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` tại phương thức `findByIdForUpdate` trong Repository.
  - Khi bắt đầu giao dịch, luồng đọc sẽ khóa dòng dữ liệu của biến thể sản phẩm tại MySQL engine (`SELECT ... FOR UPDATE`), đảm bảo việc kiểm tra tồn kho và khấu trừ số lượng là một chuỗi hành động nguyên tử (*Atomic Operation*).

### 2. Sổ Cái Biến Động Kho Toàn Vẹn (Inventory Ledger & Audit Trail)
- **Vấn đề**: Việc chỉ cập nhật một trường số `quantity` đơn lẻ dễ gây thất thoát và không thể giải trình nguồn cơn biến động kho.
- **Giải pháp**:
  - Mọi thao tác làm thay đổi số lượng kho (Bán online `SALE`, Bán quầy `POS_SALE`, Hoàn trả `RETURN`, Nhập kho `IMPORT`, Hủy hàng hỏng `DAMAGE`, Kiểm kê cân đối `INVENTORY_AUDIT`) đều bắt buộc ghi nhận 1 bản ghi vào bảng `InventoryLog`.
  - Lưu trữ đầy đủ: `quantityChange`, `previousQuantity`, `newQuantity`, `reason`, `referenceCode`, thời gian và ID tài khoản thao tác.

### 3. Tối Ưu Hóa N+1 Query & Hiệu Năng Truy Vấn (Performance Tuning)
- Mặc định toàn bộ quan hệ `@OneToMany`, `@ManyToOne` ở chế độ `FetchType.LAZY`.
- Ứng dụng `@EntityGraph(attributePaths = {"variants", "category", "brand", "images"})` hoặc `JOIN FETCH` tại các câu truy vấn danh sách, giảm số lượng câu truy vấn từ $N+1$ xuống đúng $1$ câu truy vấn duy nhất.

### 4. Khử Trùng Lặp Logic Bán Hàng (DRY Pattern in Order Processing)
- Gom chung logic xử lý danh sách món hàng (`buildOrderLines`) và ghi nhận sổ sách biến động (`writeSaleLedger`) giữa kênh Online (`createOrder`) và kênh POS tại quầy (`createPosOrder`).
- Đảm bảo tính nhất quán tuyệt đối về kiểm tra giá bán, trừ kho và tính tổng tiền trên toàn bộ các kênh bán hàng.

### 5. Phòng Ngừa Tấn Công IDOR & Chuẩn Hóa Mã Đơn Hàng
- Mã đơn hàng được sinh theo định dạng chuẩn ngày kèm mã ngẫu nhiên bảo mật cao: `ORD-YYYYMMDD-XXXXXX`.
- Ngăn chặn vòng lặp truy vấn cơ sở dữ liệu `findByOrderCode` gây nghẽn I/O.
- Bắt buộc kiểm tra quyền sở hữu người dùng (`order.getUser().getIdUser().equals(currentUser.getIdUser())`) trước khi cho phép tra cứu hoặc đánh giá sản phẩm, ngăn ngừa triệt để lỗ hổng IDOR (Insecure Direct Object Reference).

### 6. Xử Lý Webhook Thanh Toán Idempotency (Chống Double-Spending)
- Tích hợp cổng thanh toán SePay với cơ chế chốt chặn kép:
  - Khóa trùng lặp giao dịch dựa trên ràng buộc `UNIQUE (reference_code)` tại bảng `PaymentTransaction`.
  - Cơ chế **Fail-Closed Security**: Tự động từ chối mọi yêu cầu Webhook nếu biến môi trường `SEPAY_API_KEY` chưa được cấu hình an toàn trên hệ thống.
  - Xử lý mượt mà kịch bản Webhook gửi trùng lặp (*Retry Policy* của bên thứ 3) mà không làm tăng tiền hay xử lý đơn hàng hai lần.

### 7. Giới Hạn Tần Suất Truy Cập (Rate Limiting Filter)
- Bộ lọc `RateLimitingFilter` bảo vệ các endpoint nhạy cảm:
  - `/api/auth/**`: Tối đa 20 yêu cầu / phút trên mỗi địa chỉ IP (chống vét cạn mật khẩu Brute-force).
  - `/api/orders/*`: Tối đa 30 yêu cầu / phút trên mỗi địa chỉ IP (chống vét cạn mã đơn hàng tự động).
  - Trả về mã lỗi chuẩn `HTTP 429 Too Many Requests` kèm thông báo tiếng Việt rõ ràng khi vượt ngưỡng.

---

## 📂 CẤU TRÚC THƯ MỤC (PROJECT STRUCTURE)

```text
Backend/hoangbaocosmetics_backend/
├── src/
│   ├── main/
│   │   ├── java/com/hoangbaoshop/hoangbaocosmetics_backend/
│   │   │   ├── config/            # Cấu hình OpenAPI, WebMvc, Async, SePay
│   │   │   ├── controller/        # REST Controllers phân theo nghiệp vụ
│   │   │   ├── dto/               # Request/Response DTOs & PageResponse
│   │   │   ├── entity/            # JPA Entities (Hibernate mapping)
│   │   │   ├── enums/             # OrderStatus, Role, PaymentMethod, v.v.
│   │   │   ├── exception/         # Custom Exceptions & GlobalExceptionHandler
│   │   │   ├── repository/        # Spring Data JPA Repositories
│   │   │   ├── security/          # Spring Security, JWT Utils, Rate Limiting
│   │   │   └── service/           # Business Logic Interfaces & Implementations
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/com/hoangbaoshop/hoangbaocosmetics_backend/
│           ├── controller/        # Unit & Integration Tests cho Controllers
│           ├── exception/         # Tests kiểm chứng Global Exception Handler
│           ├── security/          # Tests cho JWT và RateLimitingFilter
│           └── service/           # 100% Mockito Unit Tests cho Service Layer
├── mvnw & mvnw.cmd                # Maven Wrapper
├── pom.xml                        # Cấu hình dự án và thư viện phụ thuộc
└── README.md                      # Tài liệu dự án
```

---

## 📡 TÀI LIỆU API ENDPOINTS

Hệ thống hỗ trợ tài liệu trực quan qua **Swagger UI** tại đường dẫn:  
`http://localhost:8080/swagger-ui/index.html` (hoặc `/swagger-ui.html`)

### Nhóm 1: Xác Thực & Người Dùng (`/api/auth`, `/api/users`)
- `POST /api/auth/register`: Đăng ký tài khoản khách hàng mới.
- `POST /api/auth/login`: Đăng nhập, nhận JWT Token.
- `POST /api/auth/oauth/google`: Đăng nhập bằng Google OAuth.
- `GET /api/users/profile`: Lấy thông tin tài khoản hiện tại.
- `PUT /api/users/profile`: Cập nhật thông tin và địa chỉ giao hàng.

### Nhóm 2: Danh Mục, Thương Hiệu & Sản Phẩm (`/api/categories`, `/api/brands`, `/api/products`)
- `GET /api/categories`: Danh sách danh mục sản phẩm (phân cấp).
- `GET /api/brands`: Danh sách thương hiệu mỹ phẩm.
- `GET /api/products`: Tìm kiếm, phân trang, lọc sản phẩm theo giá, danh mục, thương hiệu.
- `GET /api/products/{id}`: Chi tiết sản phẩm kèm danh sách biến thể (Variant) và hình ảnh.
- `POST /api/products`: (Admin) Tạo sản phẩm mới kèm các biến thể.
- `PUT /api/products/{id}`: (Admin) Cập nhật sản phẩm và đồng bộ biến thể theo cơ chế In-Place Merge.
- `DELETE /api/products/{id}`: (Admin) Xóa mềm sản phẩm và toàn bộ biến thể.

### Nhóm 3: Đặt Hàng & POS Quầy (`/api/orders`)
- `POST /api/orders`: Tạo đơn hàng trực tuyến (Online Checkout).
- `POST /api/orders/pos`: Tạo đơn bán lẻ tại quầy (POS - Thanh toán & trừ kho ngay).
- `GET /api/orders/{code}`: Tra cứu thông tin đơn hàng theo mã (được bảo vệ quyền sở hữu & Rate Limit).
- `GET /api/orders/my-orders`: Danh sách lịch sử đơn hàng của người dùng hiện tại.
- `PUT /api/orders/{id}/status`: (Admin) Cập nhật trạng thái đơn hàng (CONFIRMED, SHIPPING, DELIVERED, CANCELLED).

### Nhóm 4: Thanh Toán & Webhook (`/api/payment`)
- `POST /api/payment/create-qr`: Tạo mã VietQR thanh toán cho đơn hàng.
- `POST /api/payment/sepay/webhook`: Endpoint nhận Webhook xác nhận chuyển khoản ngân hàng từ SePay.

### Nhóm 5: Đánh Giá & Yêu Thích (`/api/reviews`, `/api/favorites`)
- `POST /api/reviews`: Đánh giá sản phẩm đã mua (yêu cầu đơn hàng ở trạng thái DELIVERED).
- `GET /api/reviews/product/{productId}`: Xem đánh giá và điểm trung bình của sản phẩm.
- `POST /api/favorites/toggle/{productId}`: Thêm/Xóa sản phẩm khỏi danh sách yêu thích cá nhân.
- `GET /api/favorites`: Xem danh sách sản phẩm yêu thích đã phân trang.

### Nhóm 6: Kho Bãi & Báo Cáo Thống Kê (`/api/warehouse`, `/api/dashboard`)
- `POST /api/warehouse/import`: Nhập kho sản phẩm từ Nhà cung cấp (`ImportReceipt`).
- `GET /api/warehouse/inventory-logs`: Xem lịch sử biến động kho (`InventoryLog`).
- `GET /api/dashboard/overview`: Tổng quan doanh thu, đơn hàng, tăng trưởng theo kỳ.

---

## 🔒 BẢO MẬT & PHÂN QUYỀN (SECURITY & ACCESS CONTROL)

Hệ thống phân quyền chi tiết dựa trên Role-Based Access Control (RBAC):
- **`ROLE_CUSTOMER`**: Xem sản phẩm, đặt hàng, quản lý đơn cá nhân, lưu yêu thích, đánh giá sản phẩm.
- **`ROLE_STAFF`**: Bán hàng tại quầy POS, xử lý đóng gói và chuyển trạng thái đơn hàng, tra cứu tồn kho.
- **`ROLE_ADMIN`**: Toàn quyền quản trị hệ thống, quản lý tài khoản nhân viên, cấu hình sản phẩm, nhập hàng và xem báo cáo tài chính.

---

## 🧪 CHIẾN LƯỢC KIỂM THỬ (UNIT TESTING SUITE)

Dự án áp dụng quy chuẩn kiểm thử đơn vị nghiêm ngặt sử dụng **JUnit 5** và **Mockito**:
- **Độc lập hoàn toàn với Database**: Mọi Repository, Security Context và Service phụ thuộc đều được Mock chuẩn mực, cho phép toàn bộ suite chạy hoàn tất trong dưới **10 giây**.
- **Tỷ lệ kiểm thử thành công**: **100% Pass** (72 tests, 0 failures, 0 errors).

| Test Suite | Số Test Cases | Nội Dung Kiểm Chứng Trọng Tâm |
| :--- | :---: | :--- |
| `OrderServiceImplTest` | 2 | Đặt hàng Online, Bán tại quầy POS, kiểm tra khóa kho và trừ tồn kho |
| `PaymentServiceImplTest` | 7 | Xác thực SePay API Key, Webhook Idempotency, đối soát số tiền |
| `ProductServiceImplTest` | 5 | Tìm kiếm phân trang, In-place variant merge, xóa mềm biến thể |
| `AuthServiceImplTest` | 5 | Đăng ký tài khoản mã hóa BCrypt, Đăng nhập xuất JWT, kiểm tra trùng lặp |
| `BrandServiceImplTest` | 9 | CRUD Thương hiệu, kiểm tra trùng tên, chặn xóa khi có sản phẩm liên kết |
| `SupplierServiceImplTest` | 9 | CRUD Nhà cung cấp, tìm kiếm phân trang, chặn xóa khi có phiếu nhập |
| `CategoryServiceImplTest` | 7 | CRUD Danh mục, tự động sinh Slug URL, chặn xóa khi có sản phẩm |
| `ReviewServiceImplTest` | 4 | Ràng buộc đơn DELIVERED mới được đánh giá, chặn đánh giá trùng, tính điểm sao |
| `FavoriteProductServiceImplTest` | 4 | Thêm/bỏ yêu thích Toggle, phân trang danh sách yêu thích |
| `DashboardServiceImplTest` | 2 | Tổng hợp chỉ số doanh thu, đơn hàng theo ngày/tháng |
| `WarehouseServiceImplTest` | 1 | Nhập kho, kiểm tra tăng tồn kho và ghi sổ cái biến động |
| `RateLimitingFilterTest` | 3 | Kiểm tra chặn IP khi vượt ngưỡng 20 req/phút tại Auth và 30 req/phút tại Orders |
| `JwtUtilsTest` | 2 | Ký và xác thực chữ ký số JWT, từ chối token giả mạo hoặc hết hạn |
| `GlobalExceptionHandlerTest` | 6 | Bắt và chuyển hóa ngoại lệ sang JSON chuẩn HTTP 400, 404, 409, 500 |

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & TRIỂN KHAI (SETUP & DEPLOYMENT)

### 1. Yêu Cầu Môi Trường
- **Java Development Kit (JDK)**: Phiên bản 17 LTS trở lên.
- **MySQL Server**: Phiên bản 8.0 trở lên.
- **Maven**: Đã tích hợp sẵn Maven Wrapper (`./mvnw` hoặc `.\mvnw.cmd`).

### 2. Cấu Hình Cơ Sở Dữ Liệu & Biến Môi Trường
Tạo cơ sở dữ liệu MySQL:
```sql
CREATE DATABASE hoangbao_cosmetics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cấu hình các biến môi trường cần thiết (hoặc cập nhật trong file `src/main/resources/application.properties`):
```properties
# Kết nối MySQL
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hoangbao_cosmetics?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT Secret (Tối thiểu 256 bits)
APP_JWT_SECRET=HoangBaoCosmeticsSuperSecretKeyForJWTGeneration2026SecureKeyWithHighEntropy!
APP_JWT_EXPIRATION_MS=86400000

# SePay Payment Gateway
SEPAY_API_KEY=your_sepay_api_key_here
```

### 3. Biên Dịch & Chạy Unit Tests
```powershell
# Chạy toàn bộ 72 Unit Tests
.\mvnw.cmd clean test
```

### 4. Khởi Chạy Ứng Dụng
```powershell
# Chạy ứng dụng Spring Boot
.\mvnw.cmd spring-boot:run
```

Máy chủ sẽ khởi chạy mặc định tại cổng `http://localhost:8080`.

---

## 👥 THÀNH VIÊN VÀ QUY CHUẨN ĐÓNG GÓP
- **Tech Lead & Kiến trúc sư hệ thống**: Hoàng Bảo Engineering Team.
- **Nguyên tắc phát triển**: Tuân thủ Clean Code, SOLID, DRY, Phòng thủ an ninh đa tầng (Defense in Depth) và Zero Downtime Database Migrations.
