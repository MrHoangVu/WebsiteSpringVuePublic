-- File: V3__Create_product_reviews_table.sql

CREATE TABLE dbo.ProductReviews(
                                   id BIGINT IDENTITY(1,1) NOT NULL,
                                   product_id INT NOT NULL,
                                   user_id INT NOT NULL,
                                   rating TINYINT NOT NULL,
                                   comment NVARCHAR(MAX) NULL,
                                   is_approved BIT NOT NULL CONSTRAINT DF_ProductReviews_is_approved DEFAULT 1,
                                   created_at DATETIME2(7) NOT NULL CONSTRAINT DF_ProductReviews_created_at DEFAULT GETDATE(),
                                   updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_ProductReviews_updated_at DEFAULT GETDATE(),
                                   CONSTRAINT PK_ProductReviews PRIMARY KEY CLUSTERED (id ASC),
                                   CONSTRAINT FK_ProductReviews_Product FOREIGN KEY (product_id) REFERENCES dbo.Products(id) ON DELETE CASCADE,
                                   CONSTRAINT FK_ProductReviews_User FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE,
                                   CONSTRAINT CK_ProductReviews_Rating CHECK (rating BETWEEN 1 AND 5)
);
GO

CREATE NONCLUSTERED INDEX IX_ProductReviews_ProductId ON dbo.ProductReviews(product_id);
CREATE NONCLUSTERED INDEX IX_ProductReviews_UserId ON dbo.ProductReviews(user_id);
GO