-- ==============================================================================
-- Migration V4: Dọn dẹp dữ liệu test của sản phẩm 127
-- Sản phẩm 127 ("Kem Chống Nắng La Roche-Posay Anthelios") chứa URL ảnh fake
-- và gán nhầm id_brand = 1 (3CE), gây lỗi 404 khi load ảnh trên Catalog
-- ==============================================================================

-- 1. Xóa ảnh phụ liên quan trong product_images (nếu có)
DELETE FROM `product_images` WHERE `id_product` = 127;

-- 2. Xóa biến thể trong product_variants (nếu có)
DELETE FROM `product_variants` WHERE `id_product` = 127;

-- 3. Xóa sản phẩm rác 127 trong products
DELETE FROM `products` WHERE `id_product` = 127;
