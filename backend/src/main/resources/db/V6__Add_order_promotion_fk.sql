-- File: V6__Add_order_promotion_fk.sql

ALTER TABLE dbo.Orders WITH CHECK
    ADD CONSTRAINT FK_Orders_Promotion FOREIGN KEY(promotion_id)
        REFERENCES dbo.Promotions (id)
        ON DELETE SET NULL; -- Nếu KM bị xóa, chỉ xóa liên kết trong Order
GO

ALTER TABLE dbo.Orders CHECK CONSTRAINT FK_Orders_Promotion;
GO