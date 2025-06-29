-- File: V13__Create_cart_tables.sql

-- Bảng Carts: Lưu trữ thông tin giỏ hàng chính
CREATE TABLE dbo.Carts (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           user_id INT NULL,                 -- FK đến Users, NULL cho guest
                           session_id VARCHAR(255) NULL,     -- Lưu session ID cho guest cart
                           created_at DATETIME2(7) NOT NULL CONSTRAINT DF_Carts_created_at DEFAULT GETDATE(),
                           updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_Carts_updated_at DEFAULT GETDATE(),
                           CONSTRAINT PK_Carts PRIMARY KEY CLUSTERED (id ASC),
                           CONSTRAINT FK_Carts_User FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE -- Xóa cart nếu user bị xóa
    -- Có thể thêm UNIQUE constraint cho user_id nếu mỗi user chỉ có 1 cart cố định
    -- Hoặc UNIQUE cho session_id nếu muốn đảm bảo session chỉ có 1 cart
);
GO

-- Index để tìm cart nhanh chóng
CREATE NONCLUSTERED INDEX IX_Carts_UserId ON dbo.Carts(user_id) WHERE user_id IS NOT NULL;
CREATE NONCLUSTERED INDEX IX_Carts_SessionId ON dbo.Carts(session_id) WHERE session_id IS NOT NULL;
GO


-- Bảng CartItems: Lưu trữ các sản phẩm trong giỏ hàng
CREATE TABLE dbo.CartItems (
                               id BIGINT IDENTITY(1,1) NOT NULL,
                               cart_id BIGINT NOT NULL,          -- FK đến Carts
                               product_id INT NOT NULL,          -- FK đến Products
                               quantity INT NOT NULL,
    -- Không lưu giá ở đây, luôn lấy giá mới nhất từ Products
                               created_at DATETIME2(7) NOT NULL CONSTRAINT DF_CartItems_created_at DEFAULT GETDATE(),
                               updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_CartItems_updated_at DEFAULT GETDATE(),
                               CONSTRAINT PK_CartItems PRIMARY KEY CLUSTERED (id ASC),
                               CONSTRAINT FK_CartItems_Cart FOREIGN KEY (cart_id) REFERENCES dbo.Carts(id) ON DELETE CASCADE, -- Xóa item nếu cart bị xóa
                               CONSTRAINT FK_CartItems_Product FOREIGN KEY (product_id) REFERENCES dbo.Products(id) ON DELETE CASCADE, -- Xóa item nếu product bị xóa
                               CONSTRAINT UK_CartItems_Cart_Product UNIQUE NONCLUSTERED (cart_id, product_id), -- Mỗi sản phẩm chỉ xuất hiện 1 lần/cart
                               CONSTRAINT CK_CartItems_Quantity CHECK (quantity > 0) -- Số lượng phải lớn hơn 0
);
GO

-- Index để tìm item nhanh chóng
CREATE NONCLUSTERED INDEX IX_CartItems_CartId ON dbo.CartItems(cart_id);
CREATE NONCLUSTERED INDEX IX_CartItems_ProductId ON dbo.CartItems(product_id);
GO