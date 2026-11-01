-- ==============================================================================
-- Flyway Migration V6: Thêm cột thumbnail cho bảng categories & seed dữ liệu ảnh
-- ==============================================================================

ALTER TABLE `categories`
ADD COLUMN `thumbnail` VARCHAR(255) NULL AFTER `slug`;

-- Cập nhật đường dẫn Cloudinary home-img cho các danh mục nổi bật
UPDATE `categories` SET `thumbnail` = 'home-img/son-moi/soi-moi.webp' WHERE `slug` = 'son-moi' OR `id_category` = 1;
UPDATE `categories` SET `thumbnail` = 'home-img/kem-chong-nang/kem-chong-nang.webp' WHERE `slug` = 'kem-chong-nang' OR `id_category` = 2;
UPDATE `categories` SET `thumbnail` = 'home-img/sua-rua-mat/sua-rua-mat.webp' WHERE `slug` = 'sua-rua-mat' OR `id_category` = 3;
UPDATE `categories` SET `thumbnail` = 'home-img/tinh-chat/tinh-chat.webp' WHERE `slug` = 'tinh-chat' OR `id_category` = 4;
UPDATE `categories` SET `thumbnail` = 'home-img/kem-duong/kem-duong.webp' WHERE `slug` = 'kem-duong' OR `id_category` = 5;
UPDATE `categories` SET `thumbnail` = 'home-img/tay-trang/tay-trang.webp' WHERE `slug` = 'tay-trang' OR `id_category` = 6;
UPDATE `categories` SET `thumbnail` = 'home-img/mat-na/mat-na.webp' WHERE `slug` = 'mat-na' OR `id_category` = 7;
UPDATE `categories` SET `thumbnail` = 'home-img/tay-da-chet/tay-da-chet.webp' WHERE `slug` = 'tay-da-chet' OR `id_category` = 8;
UPDATE `categories` SET `thumbnail` = 'home-img/cushion/cushion.webp' WHERE `slug` = 'cushion' OR `id_category` = 9;
UPDATE `categories` SET `thumbnail` = 'home-img/kem-lot-nen/kem-lot-nen.webp' WHERE `slug` = 'kem-lot-nen' OR `id_category` = 10;
UPDATE `categories` SET `thumbnail` = 'home-img/phan-phu/phan-phu.webp' WHERE `slug` = 'phan-phu' OR `id_category` = 11;
UPDATE `categories` SET `thumbnail` = 'home-img/kem-duong-mat/kem-duong-mat.webp' WHERE `slug` = 'kem-duong-mat' OR `id_category` = 12;
UPDATE `categories` SET `thumbnail` = 'home-img/cham-soc-co-the/cham-soc-co-the.webp' WHERE `slug` = 'cham-soc-co-the' OR `id_category` = 13;
UPDATE `categories` SET `thumbnail` = 'home-img/tpcn/tpcn.webp' WHERE `slug` = 'tpcn' OR `id_category` = 14;
UPDATE `categories` SET `thumbnail` = 'home-img/cham-soc-co-the/cham-soc-co-the.webp' WHERE `slug` = 'cham-soc-toc' OR `id_category` = 15;
UPDATE `categories` SET `thumbnail` = 'home-img/son-moi/soi-moi.webp' WHERE `slug` = 'nuoc-hoa' OR `id_category` = 16;
