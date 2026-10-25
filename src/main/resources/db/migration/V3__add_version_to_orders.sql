-- ==============================================================================
-- Migration V3: Bổ sung cột version cho bảng orders
-- Phục vụ Optimistic Locking (Khóa lạc quan) chống Race Condition
-- giữa Scheduler tự hủy đơn và Webhook thanh toán SePay
-- ==============================================================================

ALTER TABLE `orders` ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0;
