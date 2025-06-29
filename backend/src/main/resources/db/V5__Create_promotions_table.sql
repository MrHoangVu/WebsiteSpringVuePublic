-- File:V5__Create_promotions_table.sql

CREATE TABLE dbo.Promotions(
                               id INT IDENTITY(1,1) NOT NULL,
                               code VARCHAR(50) NOT NULL,
                               name NVARCHAR(255) NULL,
                               description NVARCHAR(500) NULL,
                               discount_type NVARCHAR(20) NOT NULL, -- 'PERCENTAGE' or 'FIXED_AMOUNT'
                               discount_value DECIMAL(18, 2) NOT NULL,
                               start_date DATETIME2(7) NOT NULL,
                               end_date DATETIME2(7) NOT NULL,
                               max_usage INT NULL, -- NULL for unlimited
                               current_usage INT NOT NULL CONSTRAINT DF_Promotions_current_usage DEFAULT 0,
                               min_order_value DECIMAL(18, 2) NULL, -- NULL if no minimum
                               is_active BIT NOT NULL CONSTRAINT DF_Promotions_is_active DEFAULT 1,
                               created_at DATETIME2(7) NOT NULL CONSTRAINT DF_Promotions_created_at DEFAULT GETDATE(),
                               updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_Promotions_updated_at DEFAULT GETDATE(),
                               CONSTRAINT PK_Promotions PRIMARY KEY CLUSTERED (id ASC),
                               CONSTRAINT UK_Promotions_Code UNIQUE NONCLUSTERED (code ASC),
                               CONSTRAINT CK_Promotions_DiscountType CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
                               CONSTRAINT CK_Promotions_DiscountValue CHECK (discount_value >= 0),
                               CONSTRAINT CK_Promotions_Dates CHECK (end_date >= start_date),
                               CONSTRAINT CK_Promotions_Usage CHECK (current_usage >= 0)
);
GO

CREATE NONCLUSTERED INDEX IX_Promotions_IsActive_Dates ON dbo.Promotions(is_active, start_date, end_date);
GO