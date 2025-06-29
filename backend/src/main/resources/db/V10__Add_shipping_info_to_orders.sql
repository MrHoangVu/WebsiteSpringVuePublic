-- File: V10__Add_shipping_info_to_orders.sql

-- Thêm cột lưu ID phương thức vận chuyển và phí VC
ALTER TABLE dbo.Orders
    ADD shipping_method_id INT NULL, -- Nên là NOT NULL nếu bắt buộc
        shipping_cost DECIMAL(18, 2) NOT NULL CONSTRAINT DF_Orders_shipping_cost DEFAULT 0;
GO

-- Thêm FK đến bảng ShippingMethods
ALTER TABLE dbo.Orders WITH CHECK
    ADD CONSTRAINT FK_Orders_ShippingMethod FOREIGN KEY(shipping_method_id)
        REFERENCES dbo.ShippingMethods (id)
        ON DELETE SET NULL; -- Nếu xóa phương thức VC, không xóa đơn hàng
GO
ALTER TABLE dbo.Orders CHECK CONSTRAINT FK_Orders_ShippingMethod;
GO