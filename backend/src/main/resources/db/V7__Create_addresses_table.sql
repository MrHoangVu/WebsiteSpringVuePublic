-- File: V7__Create_addresses_table.sql

CREATE TABLE dbo.Addresses(
                              id BIGINT IDENTITY(1,1) NOT NULL,
                              user_id INT NOT NULL,
                              recipient_name NVARCHAR(150) NOT NULL, -- Tăng độ dài nếu cần
                              recipient_phone VARCHAR(20) NOT NULL,
                              street_address NVARCHAR(500) NOT NULL, -- Tăng độ dài nếu cần
                              ward NVARCHAR(100) NULL,
                              district NVARCHAR(100) NOT NULL,
                              city NVARCHAR(100) NOT NULL,
                              country NVARCHAR(10) NOT NULL CONSTRAINT DF_Addresses_country DEFAULT 'VN',
                              is_default_shipping BIT NOT NULL CONSTRAINT DF_Addresses_is_default_shipping DEFAULT 0,
                              is_default_billing BIT NOT NULL CONSTRAINT DF_Addresses_is_default_billing DEFAULT 0,
                              created_at DATETIME2(7) NOT NULL CONSTRAINT DF_Addresses_created_at DEFAULT GETDATE(),
                              updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_Addresses_updated_at DEFAULT GETDATE(),
                              CONSTRAINT PK_Addresses PRIMARY KEY CLUSTERED (id ASC),
                              CONSTRAINT FK_Addresses_User FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE -- Xóa địa chỉ nếu user bị xóa
);
GO

CREATE NONCLUSTERED INDEX IX_Addresses_UserId ON dbo.Addresses(user_id);
GO