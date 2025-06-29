-- File: V4__Create_articles_table.sql

CREATE TABLE dbo.Articles(
                             id BIGINT IDENTITY(1,1) NOT NULL,
                             user_id INT NOT NULL,
                             title NVARCHAR(255) NOT NULL,
                             slug NVARCHAR(300) NOT NULL,
                             content NVARCHAR(MAX) NOT NULL,
                             excerpt NVARCHAR(500) NULL,
                             featured_image_url VARCHAR(512) NULL,
                             is_published BIT NOT NULL CONSTRAINT DF_Articles_is_published DEFAULT 0, -- Cần duyệt
                             published_at DATETIME2(7) NULL,
                             created_at DATETIME2(7) NOT NULL CONSTRAINT DF_Articles_created_at DEFAULT GETDATE(),
                             updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_Articles_updated_at DEFAULT GETDATE(),
                             CONSTRAINT PK_Articles PRIMARY KEY CLUSTERED (id ASC),
                             CONSTRAINT UK_Articles_Slug UNIQUE NONCLUSTERED (slug ASC),
                             CONSTRAINT FK_Articles_User FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_Articles_UserId ON dbo.Articles(user_id);
CREATE NONCLUSTERED INDEX IX_Articles_IsPublished ON dbo.Articles(is_published);
GO