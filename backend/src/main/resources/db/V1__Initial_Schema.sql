CREATE TABLE categories
(
    id          int IDENTITY (1, 1) NOT NULL,
    name        nvarchar(255)        NOT NULL,
    slug        nvarchar(255)        NOT NULL,
    description nvarchar(max),
    created_at  datetime            NOT NULL,
    updated_at  datetime            NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
)
GO

CREATE TABLE order_items
(
    id                int IDENTITY (1, 1) NOT NULL,
    order_id          int                 NOT NULL,
    product_id        int,
    quantity          int                 NOT NULL,
    price_at_purchase decimal(18, 2)      NOT NULL,
    CONSTRAINT pk_orderitems PRIMARY KEY (id)
)
GO

CREATE TABLE orders
(
    id               int IDENTITY (1, 1) NOT NULL,
    customer_name    nvarchar(150),
    customer_phone   nvarchar(20),
    customer_address nvarchar(500),
    total_amount     decimal(18, 2)      NOT NULL,
    status           nvarchar(50)
        CONSTRAINT DF_orders_status DEFAULT 'PENDING',
    order_date       datetime            NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
)
GO

CREATE TABLE products
(
    id          int IDENTITY (1, 1) NOT NULL,
    name        nvarchar(255)        NOT NULL,
    slug        nvarchar(255)        NOT NULL,
    description nvarchar(max),
    price       decimal(18, 2)      NOT NULL,
    stock       int
        CONSTRAINT DF_products_stock DEFAULT 0,
    image_url   nvarchar(512),
    dimensions  nvarchar(100),
    material    nvarchar(100),
    category_id int,
    created_at  datetime            NOT NULL,
    updated_at  datetime            NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
)
GO

CREATE TABLE users
(
    id         int IDENTITY (1, 1) NOT NULL,
    username   nvarchar(100)        NOT NULL,
    password   nvarchar(255)        NOT NULL,
    full_name  nvarchar(150),
    role       nvarchar(50)         NOT NULL,
    created_at datetime            NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
)
GO

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name)
GO

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_slug UNIQUE (slug)
GO

ALTER TABLE products
    ADD CONSTRAINT uc_products_slug UNIQUE (slug)
GO

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username)
GO

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDERITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id)
GO

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDERITEMS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id)
GO

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id)
GO