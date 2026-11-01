-- ==============================================================================
-- Flyway Migration V7: Thiết kế bảng Flash Sale theo chuẩn chiến dịch (Campaign)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `flash_sales` (
    `id_flash_sale` INT(11) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_flash_sale`),
    INDEX `idx_flash_sales_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `flash_sale_items` (
    `id_item` INT(11) NOT NULL AUTO_INCREMENT,
    `id_flash_sale` INT(11) NOT NULL,
    `id_variant` INT(11) NOT NULL,
    `sale_price` DECIMAL(15, 2) NOT NULL,
    `stock_quantity` INT(11) NOT NULL DEFAULT 50,
    `sold_quantity` INT(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id_item`),
    INDEX `fk_flash_sale_items_flash_sale_idx` (`id_flash_sale`),
    INDEX `fk_flash_sale_items_variant_idx` (`id_variant`),
    CONSTRAINT `fk_flash_sale_items_flash_sale` FOREIGN KEY (`id_flash_sale`) REFERENCES `flash_sales` (`id_flash_sale`) ON DELETE CASCADE,
    CONSTRAINT `fk_flash_sale_items_variant` FOREIGN KEY (`id_variant`) REFERENCES `product_variants` (`id_variant`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Seed dữ liệu chiến dịch Flash Sale mẫu đang hoạt động (kéo dài 3 ngày)
-- ------------------------------------------------------------------------------
INSERT INTO `flash_sales` (`id_flash_sale`, `title`, `start_time`, `end_time`, `status`)
VALUES (1, 'Đại Tiệc Flash Sale Mỹ Phẩm Chính Hãng', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'ACTIVE');

-- Gắn các sản phẩm hot vào đợt Flash Sale:
-- Variant 1: Cetaphil Moisturizing Cream (Gốc: 483K, Giá thường: 420K, Sale: 349K, 50 suất, Đã bán 28)
-- Variant 31: Cetaphil Pro AD Derma (Gốc: 448K, Giá thường: 390K, Sale: 319K, 40 suất, Đã bán 31)
-- Variant 36: Cocoon Thạch Hoa Hồng (Gốc: 224K, Giá thường: 195K, Sale: 159K, 60 suất, Đã bán 45)
-- Variant 50: Innisfree Kem Chống Nắng (Gốc: 414K, Giá thường: 360K, Sale: 289K, 50 suất, Đã bán 38)
-- Variant 75: La Roche-Posay B5 Plus (Gốc: 443K, Giá thường: 385K, Sale: 315K, 80 suất, Đã bán 62)
INSERT INTO `flash_sale_items` (`id_flash_sale`, `id_variant`, `sale_price`, `stock_quantity`, `sold_quantity`)
VALUES
(1, 1, 349000.00, 50, 28),
(1, 31, 319000.00, 40, 31),
(1, 36, 159000.00, 60, 45),
(1, 50, 289000.00, 50, 38),
(1, 75, 315000.00, 80, 62);
