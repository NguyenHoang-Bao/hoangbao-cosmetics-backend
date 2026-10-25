-- ==============================================================================
-- Migration V4: Bổ sung trường paid_amount cho bảng orders
-- Hỗ trợ cộng dồn các đợt chuyển tiền khi khách chuyển thiếu (UNDERPAID)
-- ==============================================================================

ALTER TABLE `orders` ADD COLUMN `paid_amount` DECIMAL(15,2) DEFAULT 0.00;
