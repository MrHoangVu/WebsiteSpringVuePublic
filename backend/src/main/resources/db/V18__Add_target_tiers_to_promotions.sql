-- File: V18__Add_target_tiers_to_promotions.sql

PRINT 'Adding target_tiers column to Promotions table...';

ALTER TABLE dbo.Promotions
    ADD target_tiers VARCHAR(100) NULL; -- Cho phép NULL nếu áp dụng cho mọi bậc
GO

PRINT 'Column target_tiers added successfully.';