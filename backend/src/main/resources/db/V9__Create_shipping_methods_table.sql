-- File: V9__Create_shipping_methods_table.sql

CREATE TABLE dbo.ShippingMethods(
                                    id INT IDENTITY(1,1) NOT NULL,
                                    name NVARCHAR(100) NOT NULL,
                                    description NVARCHAR(255) NULL,
                                    base_cost DECIMAL(18, 2) NOT NULL CONSTRAINT DF_ShippingMethods_base_cost DEFAULT 0,
                                    estimated_days_min INT NULL,
                                    estimated_days_max INT NULL,
                                    is_active BIT NOT NULL CONSTRAINT DF_ShippingMethods_is_active DEFAULT 1,
                                    CONSTRAINT PK_ShippingMethods PRIMARY KEY CLUSTERED (id ASC),
                                    CONSTRAINT UK_ShippingMethods_Name UNIQUE NONCLUSTERED (name ASC)
);
GO

-- Thêm một vài phương thức mặc định nếu muốn
INSERT INTO dbo.ShippingMethods (name, description, base_cost, estimated_days_min, estimated_days_max, is_active) VALUES
                                                                                                                      (N'Giao hàng tiêu chuẩn', N'Giao hàng dự kiến trong vòng 3-5 ngày làm việc', 30000, 3, 5, 1),
                                                                                                                      (N'Giao hàng nhanh', N'Giao hàng trong ngày hoặc ngày làm việc tiếp theo (nội thành)', 50000, 1, 2, 1);
GO