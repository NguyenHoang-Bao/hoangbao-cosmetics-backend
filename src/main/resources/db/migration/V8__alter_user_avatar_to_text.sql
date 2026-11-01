-- ==============================================================================
-- Flyway Migration V8: Mở rộng trường avatar cho người dùng hỗ trợ URL và Base64
-- ==============================================================================

ALTER TABLE `users` MODIFY COLUMN `avatar` MEDIUMTEXT DEFAULT NULL;
