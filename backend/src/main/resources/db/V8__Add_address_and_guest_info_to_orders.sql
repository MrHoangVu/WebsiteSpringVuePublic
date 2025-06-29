-- File: V8__Add_address_and_guest_info_to_orders.sql

-- Thêm cột user_id (cho phép NULL cho guest)
ALTER TABLE dbo.Orders
    ADD user_id INT NULL;
GO

-- Thêm ràng buộc FK từ Orders.user_id đến Users.id (cho phép SET NULL nếu user bị xóa)
ALTER TABLE dbo.Orders WITH CHECK
    ADD CONSTRAINT FK_Orders_User FOREIGN KEY(user_id)
        REFERENCES dbo.Users (id)
        ON DELETE SET NULL; -- Nếu user bị xóa, giữ lại đơn hàng nhưng user_id thành NULL
GO

ALTER TABLE dbo.Orders CHECK CONSTRAINT FK_Orders_User;
GO

-- Thêm cột email cho guest
ALTER TABLE dbo.Orders
    ADD guest_email VARCHAR(255) NULL;
GO

-- Thêm cột lưu ID địa chỉ đã chọn (cho user đăng nhập)
ALTER TABLE dbo.Orders
    ADD shipping_address_id BIGINT NULL,
        billing_address_id BIGINT NULL; -- Có thể chỉ cần shipping
GO

-- Thêm ràng buộc FK (giả sử không xóa address, hoặc xử lý logic khi address bị xóa)
ALTER TABLE dbo.Orders WITH CHECK
    ADD CONSTRAINT FK_Orders_ShippingAddress FOREIGN KEY(shipping_address_id)
        REFERENCES dbo.Addresses (id)
        ON DELETE NO ACTION; -- Hoặc SET NULL nếu muốn
GO
ALTER TABLE dbo.Orders CHECK CONSTRAINT FK_Orders_ShippingAddress;
GO

-- Tương tự cho billing_address_id nếu có
-- ALTER TABLE dbo.Orders WITH CHECK ADD CONSTRAINT FK_Orders_BillingAddress...

-- Thêm các cột để copy/lưu địa chỉ cuối cùng của đơn hàng (quan trọng để giữ lịch sử đơn hàng)
ALTER TABLE dbo.Orders
    ADD shipping_recipient_name NVARCHAR(150) NULL,
        shipping_recipient_phone VARCHAR(20) NULL,
        shipping_street_address NVARCHAR(500) NULL,
        shipping_ward NVARCHAR(100) NULL,
        shipping_district NVARCHAR(100) NULL,
        shipping_city NVARCHAR(100) NULL;
GO

-- Cân nhắc làm cho các cột customer_* cũ nullable hoặc bỏ đi nếu không dùng nữa
-- Ví dụ:
-- ALTER TABLE dbo.Orders ALTER COLUMN customer_name NVARCHAR(150) NULL;
-- ALTER TABLE dbo.Orders ALTER COLUMN customer_phone VARCHAR(20) NULL;
-- ALTER TABLE dbo.Orders ALTER COLUMN customer_address NVARCHAR(500) NULL;
-- Hoặc:
-- ALTER TABLE dbo.Orders DROP COLUMN customer_name, customer_phone, customer_address;
-- LƯU Ý: DROP COLUMN là thay đổi phá vỡ, cần cập nhật Entity Java tương ứng!
-- Tạm thời nên giữ lại và cho phép NULL.