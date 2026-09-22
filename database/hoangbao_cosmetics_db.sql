
CREATE DATABASE IF NOT EXISTS `hoangbao_cosmetics_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `hoangbao_cosmetics_db`;

-- ==========================================
-- 1. NHÓM TÀI KHOẢN & PHÂN QUYỀN
-- ==========================================
CREATE TABLE `roles` (
  `id_role` int(11) NOT NULL AUTO_INCREMENT,
  `name_role` varchar(50) NOT NULL UNIQUE, -- ADMIN, ONLINE_STAFF, STAFF, WAREHOUSE_MANAGER, CUSTOMER
  PRIMARY KEY (`id_role`)
) ENGINE=InnoDB;

CREATE TABLE `users` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) UNIQUE NOT NULL,
  `password` varchar(512) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `email` varchar(255) UNIQUE DEFAULT NULL,
  `phone_number` varchar(20) UNIQUE DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `enabled` bit(1) DEFAULT b'1',
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB;

CREATE TABLE `user_roles` (
  `id_user` int(11) NOT NULL,
  `id_role` int(11) NOT NULL,
  PRIMARY KEY (`id_user`, `id_role`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_role`) REFERENCES `roles` (`id_role`)
) ENGINE=InnoDB;

-- ==========================================
-- 2. NHÓM SẢN PHẨM MỸ PHẨM[cite: 1]
-- ==========================================
CREATE TABLE `categories` (
  `id_category` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL, -- Chăm sóc da, Tóc & Cơ thể...[cite: 1]
  `slug` varchar(255) UNIQUE NOT NULL,
  PRIMARY KEY (`id_category`)
) ENGINE=InnoDB;

CREATE TABLE `brands` (
  `id_brand` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL, -- Cocoon, CeraVe, Innisfree...[cite: 1]
  `logo_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_brand`)
) ENGINE=InnoDB;

CREATE TABLE `products` (
  `id_product` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `id_category` int(11) NOT NULL,
  `id_brand` int(11) NOT NULL,
  `description` longtext DEFAULT NULL,
  `benefits` text DEFAULT NULL, -- Lưu JSON array các công dụng[cite: 1]
  `key_ingredients` text DEFAULT NULL, -- Lưu JSON array thành phần[cite: 1]
  `usage_instruction` text DEFAULT NULL, -- Hướng dẫn sử dụng[cite: 1]
  `thumbnail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_product`),
  FOREIGN KEY (`id_category`) REFERENCES `categories` (`id_category`),
  FOREIGN KEY (`id_brand`) REFERENCES `brands` (`id_brand`)
) ENGINE=InnoDB;

-- Bảng Biến thể: Quản lý dung tích (ml/g) như size quần áo[cite: 1]
CREATE TABLE `product_variants` (
  `id_variant` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `sku` varchar(100) UNIQUE NOT NULL, -- Ví dụ: CCN-MIC-BIDAO-140[cite: 1]
  `capacity` varchar(50) NOT NULL, -- 140ml, 500ml, 50g[cite: 1]
  `original_price` double NOT NULL,
  `price` double NOT NULL,
  `stock_quantity` int(11) DEFAULT 0,
  PRIMARY KEY (`id_variant`),
  FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`)
) ENGINE=InnoDB;

CREATE TABLE `product_images` (
  `id_image` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  PRIMARY KEY (`id_image`),
  FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`)
) ENGINE=InnoDB;

-- ==========================================
-- 3. NHÓM QUẢN LÝ KHO THỰC TẾ (WAREHOUSE)
-- ==========================================
CREATE TABLE `suppliers` (
  `id_supplier` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_supplier`)
) ENGINE=InnoDB;

-- Phiếu nhập kho
CREATE TABLE `import_receipts` (
  `id_receipt` int(11) NOT NULL AUTO_INCREMENT,
  `id_supplier` int(11) NOT NULL,
  `id_warehouse_manager` int(11) NOT NULL, -- Người lập phiếu
  `import_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `total_cost` double NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_receipt`),
  FOREIGN KEY (`id_supplier`) REFERENCES `suppliers` (`id_supplier`),
  FOREIGN KEY (`id_warehouse_manager`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB;

-- Chi tiết phiếu nhập
CREATE TABLE `import_receipt_details` (
  `id_receipt` int(11) NOT NULL,
  `id_variant` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `import_price` double NOT NULL,
  PRIMARY KEY (`id_receipt`, `id_variant`),
  FOREIGN KEY (`id_receipt`) REFERENCES `import_receipts` (`id_receipt`),
  FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`)
) ENGINE=InnoDB;

-- Sổ cái lưu vết biến động kho (Audit Log)
CREATE TABLE `inventory_logs` (
  `id_log` int(11) NOT NULL AUTO_INCREMENT,
  `id_variant` int(11) NOT NULL,
  `id_user` int(11) NOT NULL, -- Người thực hiện thao tác
  `transaction_type` enum('IMPORT', 'EXPORT_SALE', 'RETURN', 'ADJUSTMENT') NOT NULL,
  `quantity_change` int(11) NOT NULL, -- Dương là nhập, Âm là xuất
  `reference_id` int(11) DEFAULT NULL, -- Chứa id_order hoặc id_receipt tùy loại giao dịch
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_log`),
  FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB;

-- ==========================================
-- 4. NHÓM ĐƠN HÀNG & THANH TOÁN (HỖ TRỢ KHÁCH VÃNG LAI)
-- ==========================================
CREATE TABLE `orders` (
  `id_order` int(11) NOT NULL AUTO_INCREMENT,
  `order_code` varchar(50) UNIQUE NOT NULL, -- VD: HBC-82914[cite: 1]
  `id_user` int(11) DEFAULT NULL, -- NULL nếu là khách vãng lai mua tại quầy
  `id_staff` int(11) DEFAULT NULL, -- Nhân viên tạo đơn (nếu mua tại quầy)
  `order_type` enum('ONLINE', 'OFFLINE') NOT NULL, 
  
  -- Thông tin giao hàng (có thể null nếu mua Offline)
  `customer_name` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `delivery_address` varchar(500) DEFAULT NULL,
  
  -- Thông tin tài chính
  `total_product_price` double NOT NULL,
  `shipping_fee` double DEFAULT 0,
  `total_amount` double NOT NULL,
  
  -- Cổng thanh toán & Webhook
  `payment_method` enum('COD', 'VNPAY', 'MOMO', 'CASH') NOT NULL, 
  `transaction_id` varchar(255) DEFAULT NULL, -- Mã giao dịch từ cổng thanh toán
  `webhook_status` varchar(100) DEFAULT NULL, -- SUCCESS, FAILED, PENDING
  
  `status` enum('PENDING', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED') NOT NULL,
  `note` text DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_order`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_staff`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB;

CREATE TABLE `order_details` (
  `id_order_detail` int(11) NOT NULL AUTO_INCREMENT,
  `id_order` int(11) NOT NULL,
  `id_variant` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` double NOT NULL, -- Giá bán tại thời điểm mua
  `is_reviewed` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id_order_detail`),
  FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`),
  FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`)
) ENGINE=InnoDB;

-- ==========================================
-- 5. NHÓM TƯƠNG TÁC (REVIEW, FEEDBACK, FAVORITE)
-- ==========================================
CREATE TABLE `reviews` (
  `id_review` int(11) NOT NULL AUTO_INCREMENT,
  `id_product` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_order_detail` int(11) NOT NULL UNIQUE,
  `rating_point` float NOT NULL, -- 1.0 đến 5.0[cite: 1]
  `content` text DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_review`),
  FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_order_detail`) REFERENCES `order_details` (`id_order_detail`)
) ENGINE=InnoDB;

CREATE TABLE `favorite_products` (
  `id_user` int(11) NOT NULL,
  `id_product` int(11) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`, `id_product`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_product`) REFERENCES `products` (`id_product`)
) ENGINE=InnoDB;

-- Bảng Feedback bám sát Contact Form trên React[cite: 1]
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
) ENGINE=InnoDB;

-- ==========================================
-- 6. NHÓM BÀI VIẾT, CHUYÊN MỤC & CẨM NANG HOẠT CHẤT (BLOG & GUIDES)
-- ==========================================
CREATE TABLE IF NOT EXISTS `blog_categories` (
  `id_category` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id_category`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `blog_authors` (
  `id_author` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `department` varchar(255) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  PRIMARY KEY (`id_author`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `articles` (
  `id_article` varchar(100) NOT NULL,
  `title` varchar(500) NOT NULL,
  `subtitle` text DEFAULT NULL,
  `id_category` varchar(50) NOT NULL,
  `id_author` varchar(50) NOT NULL,
  `published_date` varchar(50) DEFAULT NULL,
  `time_published` varchar(20) DEFAULT NULL,
  `read_time` varchar(50) DEFAULT NULL,
  `views` int(11) DEFAULT 0,
  `thumbnail` varchar(500) DEFAULT NULL,
  `photo_credit` varchar(255) DEFAULT NULL,
  `summary` text DEFAULT NULL,
  `pull_quote` text DEFAULT NULL,
  `tags` text DEFAULT NULL,
  `content_intro` text DEFAULT NULL,
  `content_conclusion` text DEFAULT NULL,
  `recommended_products` text DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_article`),
  FOREIGN KEY (`id_category`) REFERENCES `blog_categories` (`id_category`),
  FOREIGN KEY (`id_author`) REFERENCES `blog_authors` (`id_author`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `ingredient_guides` (
  `id_guide` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `scientific_name` varchar(255) NOT NULL,
  `role` text NOT NULL,
  `suitable_for` text NOT NULL,
  `note` text DEFAULT NULL,
  `dosage` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_guide`)
) ENGINE=InnoDB;


