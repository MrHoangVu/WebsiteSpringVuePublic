-- File: V17__Add_customer_tier_and_spending.sql

PRINT 'Adding customer tier and total spent columns to Users table...';

ALTER TABLE dbo.Users
    ADD total_spent DECIMAL(18, 2) NOT NULL CONSTRAINT DF_Users_total_spent DEFAULT 0, -- Tổng chi tiêu
    tier VARCHAR(20) NOT NULL CONSTRAINT DF_Users_tier DEFAULT 'BRONZE', -- Bậc khách hàng (BRONZE, SILVER, GOLD, DIAMOND)
    is_active BIT NOT NULL CONSTRAINT DF_Users_is_active DEFAULT 1; -- Thêm cột trạng thái hoạt động cho user
GO

-- Thêm CHECK constraint cho cột tier (tùy chọn nhưng nên có)
ALTER TABLE dbo.Users
    ADD CONSTRAINT CK_Users_Tier CHECK (tier IN ('BRONZE', 'SILVER', 'GOLD', 'DIAMOND'));
GO

-- (Tùy chọn) Tạo index cho cột tier và is_active để lọc nhanh hơn
CREATE NONCLUSTERED INDEX IX_Users_Tier ON dbo.Users(tier);
CREATE NONCLUSTERED INDEX IX_Users_IsActive ON dbo.Users(is_active);
GO

PRINT 'Columns added and constraints/indexes created successfully.';