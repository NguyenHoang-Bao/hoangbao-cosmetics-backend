-- ==============================================================================
-- HOÀNG BẢO COSMETICS - DATABASE SCHEMA DEFINITION (DDL)
-- Phiên bản: 2.0 (Chuẩn hóa toàn diện: E-commerce, POS Retail, VietQR, Voucher, Audit)
-- 
-- 4 Điểm Cốt Lõi Đã Bổ Sung:
--   1. Phương thức thanh toán VIETQR & BANK_TRANSFER (Hỗ trợ SePay & Chuyển khoản QR).
--   2. Cờ is_active trên products & product_variants (Soft Delete / Ngừng kinh doanh không lỗi FK).
--   3. Bảng coupons và trường discount_amount, coupon_code trên orders (Mã giảm giá).
--   4. Cột updated_at tự động trên orders và products (Theo dõi biến động & Webhook n8n).
--
-- Hướng dẫn chạy thủ công nếu cần tạo DB riêng ở máy local:
--    CREATE DATABASE IF NOT EXISTS `hoangbao_cosmetics_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--    USE `hoangbao_cosmetics_db`;
-- ==============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. NHÓM TÀI KHOẢN & PHÂN QUYỀN
-- ==========================================
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;

CREATE TABLE `roles` (
  `id_role` int(11) NOT NULL AUTO_INCREMENT,
  `name_role` varchar(50) NOT NULL UNIQUE, -- ADMIN, ONLINE_STAFF, STAFF, WAREHOUSE_MANAGER, CUSTOMER
  PRIMARY KEY (`id_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL UNIQUE,
  `password` varchar(512) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL UNIQUE,
  `phone_number` varchar(20) DEFAULT NULL UNIQUE,
  `avatar` varchar(255) DEFAULT NULL,
  `enabled` bit(1) DEFAULT b'1',
  `email_verified` bit(1) DEFAULT b'0',
  `loyalty_points` int(11) DEFAULT 0,
  `auth_provider` varchar(20) DEFAULT 'LOCAL',
  `provider_id` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_roles` (
  `id_user` int(11) NOT NULL,
  `id_role` int(11) NOT NULL,
  PRIMARY KEY (`id_user`, `id_role`),
  KEY `idx_user_roles_role` (`id_role`),
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`id_role`) REFERENCES `roles` (`id_role`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. NHÓM SẢN PHẨM MỸ PHẨM & BIẾN THỂ
-- ==========================================
DROP TABLE IF EXISTS `product_images`;
DROP TABLE IF EXISTS `product_variants`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `brands`;
DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `id_category` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL UNIQUE,
  PRIMARY KEY (`id_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `brands` (
  `id_brand` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_brand`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `products` (
  `id_product` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `id_category` int(11) NOT NULL,
  `id_brand` int(11) NOT NULL,
  `description` longtext DEFAULT NULL,
  `benefits` text DEFAULT NULL, -- JSON array các công dụng nổi bật
  `key_ingredients` text DEFAULT NULL, -- JSON array các thành phần chính
  `usage_instruction` text DEFAULT NULL, -- Hướng dẫn sử dụng
  `thumbnail` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT b'1', -- Cờ ẩn/hiện sản phẩm (Soft Delete)
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_product`),
  KEY `idx_products_category` (`id_category`),
  KEY `idx_products_brand` (`id_brand`),
  KEY `idx_products_active` (`is_active`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`id_category`) REFERENCES `categories` (`id_category`),
  CONSTRAINT `fk_products_brand` FOREIGN KEY (`id_brand`) REFERENCES `brands` (`id_brand`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Biến thể: Quản lý theo dung tích (ml/g), mã SKU, mã vạch POS và phân bổ tồn quầy kệ vs kho tổng
CREATE TABLE `product_variants` (
  `id_variant` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `sku` varchar(100) NOT NULL UNIQUE, -- Ví dụ: CCN-MIC-BIDAO-140 (Mã quản lý nội bộ)
  `barcode` varchar(100) DEFAULT NULL, -- Mã vạch EAN-13 quét tại máy POS bán lẻ
  `capacity` varchar(50) NOT NULL,    -- 140ml, 500ml, 50g
  `original_price` decimal(15, 0) NOT NULL, -- Giá gốc/niêm yết (VNĐ)
  `price` decimal(15, 0) NOT NULL,          -- Giá bán thực tế (VNĐ)
  `stock_quantity` int(11) DEFAULT 0,       -- Tổng tồn kho (shelf_stock + central_stock)
  `shelf_stock` int(11) DEFAULT 0,          -- Tồn thực tế trên quầy kệ trưng bày
  `central_stock` int(11) DEFAULT 0,        -- Tồn trong kho tổng / kho dự trữ
  `shelf_min_alert` int(11) DEFAULT 5,      -- Ngưỡng báo động cần châm hàng ra kệ
  `shelf_capacity` int(11) DEFAULT 20,      -- Sức chứa tối đa của vị trí trưng bày trên kệ
  `is_active` bit(1) DEFAULT b'1',          -- Cờ ẩn/hiện biến thể (Soft Delete)
  PRIMARY KEY (`id_variant`),
  KEY `idx_variants_product` (`id_product`),
  KEY `idx_variants_barcode` (`barcode`),
  KEY `idx_variants_active` (`is_active`),
  KEY `idx_variants_stock` (`is_active`, `stock_quantity`),
  CONSTRAINT `fk_variants_product` FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `product_images` (
  `id_image` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  PRIMARY KEY (`id_image`),
  KEY `idx_images_product` (`id_product`),
  CONSTRAINT `fk_images_product` FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. NHÓM QUẢN LÝ KHO THỰC TẾ (WAREHOUSE & LOGS)
-- ==========================================
DROP TABLE IF EXISTS `replenishment_slip_details`;
DROP TABLE IF EXISTS `replenishment_slips`;
DROP TABLE IF EXISTS `inventory_logs`;
DROP TABLE IF EXISTS `import_receipt_details`;
DROP TABLE IF EXISTS `import_receipts`;
DROP TABLE IF EXISTS `suppliers`;

CREATE TABLE `suppliers` (
  `id_supplier` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_supplier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phiếu nhập kho từ nhà cung cấp
CREATE TABLE `import_receipts` (
  `id_receipt` int(11) NOT NULL AUTO_INCREMENT,
  `id_supplier` int(11) NOT NULL,
  `id_warehouse_manager` int(11) NOT NULL, -- Người lập phiếu nhập kho
  `import_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `total_cost` decimal(15, 0) NOT NULL,    -- Tổng chi phí nhập hàng (VNĐ)
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_receipt`),
  KEY `idx_receipts_supplier` (`id_supplier`),
  KEY `idx_receipts_manager` (`id_warehouse_manager`),
  CONSTRAINT `fk_receipts_supplier` FOREIGN KEY (`id_supplier`) REFERENCES `suppliers` (`id_supplier`),
  CONSTRAINT `fk_receipts_manager` FOREIGN KEY (`id_warehouse_manager`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chi tiết phiếu nhập theo từng biến thể (Hỗ trợ Lô hàng LOT, Hạn sử dụng EXP & Phân bổ tồn kệ vs kho tổng)
CREATE TABLE `import_receipt_details` (
  `id_receipt` int(11) NOT NULL,
  `id_variant` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `shelf_quantity` int(11) DEFAULT 0,       -- Số lượng chuyển thẳng ra quầy kệ ngay khi nhập
  `central_quantity` int(11) DEFAULT 0,     -- Số lượng lưu trữ tại kho tổng
  `import_price` decimal(15, 0) NOT NULL,  -- Đơn giá nhập tại thời điểm nhập (VNĐ)
  `batch_number` varchar(50) DEFAULT NULL,  -- Số lô sản xuất (LOT)
  `expiry_date` date DEFAULT NULL,          -- Hạn sử dụng của lô mỹ phẩm (EXP Date)
  PRIMARY KEY (`id_receipt`, `id_variant`),
  KEY `idx_receipt_details_variant` (`id_variant`),
  KEY `idx_receipt_details_expiry` (`expiry_date`),
  CONSTRAINT `fk_receipt_details_receipt` FOREIGN KEY (`id_receipt`) REFERENCES `import_receipts` (`id_receipt`) ON DELETE CASCADE,
  CONSTRAINT `fk_receipt_details_variant` FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sổ cái lưu vết biến động kho (Audit Log chuyên nghiệp)
CREATE TABLE `inventory_logs` (
  `id_log` int(11) NOT NULL AUTO_INCREMENT,
  `id_variant` int(11) NOT NULL,
  `id_user` int(11) NOT NULL, -- Người thực hiện thao tác
  `transaction_type` enum('IMPORT', 'EXPORT_SALE', 'RETURN', 'ADJUSTMENT', 'TRANSFER') NOT NULL,
  `quantity_change` int(11) NOT NULL, -- Dương: nhập vào, Âm: xuất ra
  `reference_id` int(11) DEFAULT NULL, -- Mã id_order, id_receipt hoặc id_slip tham chiếu
  `note` varchar(255) DEFAULT NULL,   -- Diễn giải chi tiết lý do biến động
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_log`),
  KEY `idx_inv_logs_variant` (`id_variant`),
  KEY `idx_inv_logs_user` (`id_user`),
  KEY `idx_inv_logs_created` (`created_at`),
  CONSTRAINT `fk_inv_logs_variant` FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`),
  CONSTRAINT `fk_inv_logs_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phiếu điều chuyển nội bộ (Châm hàng từ Kho tổng ra Quầy kệ - Auto Replenishment)
CREATE TABLE `replenishment_slips` (
  `id_slip` int(11) NOT NULL AUTO_INCREMENT,
  `slip_code` varchar(50) NOT NULL UNIQUE, -- Ví dụ: DC-20261008-01
  `id_performer` int(11) NOT NULL,          -- Người thực hiện (Thủ kho)
  `total_items` int(11) NOT NULL DEFAULT 0, -- Tổng số mặt hàng
  `total_quantity` int(11) NOT NULL DEFAULT 0, -- Tổng số lượng sản phẩm châm hàng
  `status` enum('PENDING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED',
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_slip`),
  KEY `idx_replenishment_performer` (`id_performer`),
  CONSTRAINT `fk_replenishment_performer` FOREIGN KEY (`id_performer`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `replenishment_slip_details` (
  `id_detail` int(11) NOT NULL AUTO_INCREMENT,
  `id_slip` int(11) NOT NULL,
  `id_variant` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  PRIMARY KEY (`id_detail`),
  KEY `idx_slip_details_slip` (`id_slip`),
  KEY `idx_slip_details_variant` (`id_variant`),
  CONSTRAINT `fk_slip_details_slip` FOREIGN KEY (`id_slip`) REFERENCES `replenishment_slips` (`id_slip`) ON DELETE CASCADE,
  CONSTRAINT `fk_slip_details_variant` FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. NHÓM KHUYẾN MÃI (COUPONS & VOUCHERS)
-- ==========================================
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `id_coupon` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL UNIQUE,       -- Ví dụ: WELCOME10, FREESHIP, HBCOSMETICS50K
  `description` varchar(255) DEFAULT NULL,   -- Mô tả khuyến mãi
  `discount_type` enum('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL, -- Giảm % hoặc tiền mặt
  `discount_value` decimal(15, 0) NOT NULL,  -- 10 (cho 10%) hoặc 50000 (cho 50.000 VNĐ)
  `min_order_amount` decimal(15, 0) DEFAULT 0, -- Giá trị đơn tối thiểu để dùng
  `max_discount_amount` decimal(15, 0) DEFAULT NULL, -- Giảm tối đa (áp dụng cho %)
  `usage_limit` int(11) DEFAULT NULL,        -- Giới hạn số lượt dùng (NULL: không giới hạn)
  `used_count` int(11) DEFAULT 0,            -- Số lượt đã dùng
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `is_active` bit(1) DEFAULT b'1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_coupon`),
  KEY `idx_coupons_code` (`code`),
  KEY `idx_coupons_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. NHÓM ĐƠN HÀNG & BÁN LẺ (POS & ONLINE)
-- ==========================================
DROP TABLE IF EXISTS `order_details`;
DROP TABLE IF EXISTS `orders`;

CREATE TABLE `orders` (
  `id_order` int(11) NOT NULL AUTO_INCREMENT,
  `order_code` varchar(50) NOT NULL UNIQUE, -- Mã đơn hàng hiển thị (VD: HBC-82914)
  `id_user` int(11) DEFAULT NULL,           -- NULL nếu là khách vãng lai mua tại quầy POS
  `id_staff` int(11) DEFAULT NULL,          -- Nhân viên thu ngân/bán hàng tạo đơn
  `order_type` enum('ONLINE', 'OFFLINE') NOT NULL,
  
  -- Thông tin nhận hàng (hỗ trợ cả khách không đăng nhập tài khoản)
  `customer_name` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `delivery_address` varchar(500) DEFAULT NULL,
  
  -- Thông tin tài chính & Khuyến mãi (VNĐ)
  `total_product_price` decimal(15, 0) NOT NULL,
  `discount_amount` decimal(15, 0) DEFAULT 0,    -- Số tiền được giảm
  `coupon_code` varchar(50) DEFAULT NULL,         -- Mã coupon áp dụng
  `shipping_fee` decimal(15, 0) DEFAULT 0,
  `total_amount` decimal(15, 0) NOT NULL,
  
  -- Thanh toán & Tích hợp Webhook (Hỗ trợ VietQR, SePay, MoMo, VNPay, COD, Tiền mặt)
  `payment_method` enum('COD', 'VNPAY', 'MOMO', 'CASH', 'BANK_TRANSFER', 'VIETQR') NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL, -- Mã giao dịch cổng thanh toán / ngân hàng
  `webhook_status` varchar(100) DEFAULT NULL, -- SUCCESS, FAILED, PENDING
  
  `status` enum('PENDING', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED') NOT NULL,
  `note` text DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_order`),
  UNIQUE KEY `uk_orders_transaction` (`transaction_id`),
  KEY `idx_orders_user` (`id_user`),
  KEY `idx_orders_staff` (`id_staff`),
  KEY `idx_orders_status` (`status`),
  KEY `idx_orders_created` (`created_at`),
  KEY `idx_orders_status_created` (`status`, `created_at`),
  KEY `idx_orders_updated` (`updated_at`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `fk_orders_staff` FOREIGN KEY (`id_staff`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order_details` (
  `id_order_detail` int(11) NOT NULL AUTO_INCREMENT,
  `id_order` int(11) NOT NULL,
  `id_variant` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(15, 0) NOT NULL, -- Giá bán chốt tại thời điểm mua hàng
  `is_reviewed` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id_order_detail`),
  KEY `idx_order_details_order` (`id_order`),
  KEY `idx_order_details_variant` (`id_variant`),
  CONSTRAINT `fk_order_details_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_details_variant` FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 6. NHÓM TƯƠNG TÁC (REVIEW, FAVORITE, FEEDBACK)
-- ==========================================
DROP TABLE IF EXISTS `feedbacks`;
DROP TABLE IF EXISTS `favorite_products`;
DROP TABLE IF EXISTS `reviews`;

CREATE TABLE `reviews` (
  `id_review` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_order_detail` int(11) NOT NULL UNIQUE,
  `rating_point` float NOT NULL, -- Điểm đánh giá (1.0 đến 5.0 sao)
  `content` text DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_review`),
  KEY `idx_reviews_product` (`id_product`),
  KEY `idx_reviews_user` (`id_user`),
  CONSTRAINT `fk_reviews_product` FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`),
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `fk_reviews_order_detail` FOREIGN KEY (`id_order_detail`) REFERENCES `order_details` (`id_order_detail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favorite_products` (
  `id_user` int(11) NOT NULL,
  `id_product` int(11) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`, `id_product`),
  KEY `idx_fav_product` (`id_product`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_product` FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedbacks` (
  `id_feedback` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `is_read` bit(1) DEFAULT b'0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_feedback`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `otp_tokens` (
  `id_otp` bigint NOT NULL AUTO_INCREMENT,
  `attempt_count` int NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_time` datetime(6) NOT NULL,
  `is_used` bit(1) NOT NULL DEFAULT b'0',
  `otp_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_type` enum('FORGOT_PASSWORD','REGISTER_VERIFY') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_otp`),
  KEY `idx_otp_email_type` (`email`, `token_type`),
  KEY `idx_otp_code` (`otp_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- HOÀN TẤT KHỞI TẠO CẤU TRÚC (SCHEMA V2.0 READY)
-- ==============================================================================
