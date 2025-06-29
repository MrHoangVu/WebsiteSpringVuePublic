-- File: V11__Add_payment_method_to_orders.sql

-- Thêm cột lưu phương thức thanh toán
ALTER TABLE dbo.Orders
    ADD payment_method VARCHAR(50) NULL; -- Nên là NOT NULL nếu bắt buộc
GO

-- Cập nhật CHECK constraint cho status nếu cần thêm các trạng thái thanh toán
-- Ví dụ: Bỏ constraint cũ và thêm lại constraint mới
-- ALTER TABLE dbo.Orders DROP CONSTRAINT [Tên constraint status cũ]; -- Thay bằng tên constraint thực tế
-- GO
-- ALTER TABLE dbo.Orders WITH CHECK ADD CONSTRAINT CK_Orders_Status CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED', 'PAYMENT_PENDING', 'PAID', 'DELIVERED')); -- Thêm các trạng thái mới
-- GO