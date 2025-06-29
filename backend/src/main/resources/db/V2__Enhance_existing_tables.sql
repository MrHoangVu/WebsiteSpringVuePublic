-- File: V2__Enhance_existing_tables.sql

-- --- Bảng Products ---
-- Thêm cột is_active để quản lý hiển thị sản phẩm
ALTER TABLE dbo.Products
    ADD is_active BIT NOT NULL CONSTRAINT DF_Products_is_active DEFAULT 1;
GO

-- Chuẩn hóa NOT NULL cho timestamps (Chỉ chạy nếu bạn chắc chắn cột chưa có giá trị NULL)
-- Hoặc cập nhật các giá trị NULL thành GETDATE() trước nếu cần
ALTER TABLE dbo.Products ALTER COLUMN created_at DATETIME2(7) NOT NULL;
ALTER TABLE dbo.Products ALTER COLUMN updated_at DATETIME2(7) NOT NULL;
GO
-- Thay đổi kiểu ntext sang nvarchar(max)
ALTER TABLE dbo.Products ALTER COLUMN description NVARCHAR(MAX) NULL;
GO


-- --- Bảng Categories ---
-- Chuẩn hóa NOT NULL cho timestamps
ALTER TABLE dbo.Categories ALTER COLUMN created_at DATETIME2(7) NOT NULL;
ALTER TABLE dbo.Categories ALTER COLUMN updated_at DATETIME2(7) NOT NULL;
GO
-- Thay đổi kiểu ntext sang nvarchar(max)
ALTER TABLE dbo.Categories ALTER COLUMN description NVARCHAR(MAX) NULL;
GO

-- --- Bảng Orders ---
-- Thêm cột để lưu thông tin khuyến mãi (FK sẽ thêm sau khi có bảng Promotions)
ALTER TABLE dbo.Orders
    ADD promotion_id INT NULL, -- Cho phép NULL nếu không áp dụng KM
        discount_amount DECIMAL(18, 2) NOT NULL CONSTRAINT DF_Orders_discount_amount DEFAULT 0; -- Số tiền giảm giá thực tế
GO
-- Chuẩn hóa order_date
ALTER TABLE dbo.Orders ALTER COLUMN order_date DATETIME2(7) NOT NULL;
GO

-- --- Bảng Users ---
-- Chuẩn hóa created_at
ALTER TABLE dbo.Users ALTER COLUMN created_at DATETIME2(7) NOT NULL;
GO
