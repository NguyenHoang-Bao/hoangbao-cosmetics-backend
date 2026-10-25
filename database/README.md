# Database Migrations & Schemas

> **Lưu ý quan trọng**:
> Toàn bộ cấu trúc cơ sở dữ liệu (schema) và dữ liệu khởi tạo (seed data) của hệ thống được quản lý tự động và tập trung duy nhất thông qua **Flyway Migration**.
>
> Vui lòng không thực thi trực tiếp các file SQL thủ công trên môi trường Development hoặc Production. Mọi thay đổi schema/data phải được viết dưới dạng migration script mới tại thư mục:
> `src/main/resources/db/migration/` (Ví dụ: `V1__init_schema.sql`, `V2__seed_initial_data.sql`, ...)
