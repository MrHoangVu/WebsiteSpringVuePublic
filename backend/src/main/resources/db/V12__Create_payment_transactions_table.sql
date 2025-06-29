-- File: V12__Create_payment_transactions_table.sql

CREATE TABLE dbo.PaymentTransactions(
                                        id BIGINT IDENTITY(1,1) NOT NULL,
                                        order_id INT NOT NULL,
                                        gateway_transaction_id VARCHAR(255) NULL,
                                        payment_gateway VARCHAR(50) NOT NULL,
                                        amount DECIMAL(18, 2) NOT NULL,
                                        status VARCHAR(50) NOT NULL, -- PENDING, SUCCESS, FAILED, CANCELED,...
                                        transaction_time DATETIME2(7) NOT NULL,
                                        raw_response NVARCHAR(MAX) NULL,
                                        created_at DATETIME2(7) NOT NULL CONSTRAINT DF_PaymentTransactions_created_at DEFAULT GETDATE(),
                                        CONSTRAINT PK_PaymentTransactions PRIMARY KEY CLUSTERED (id ASC),
                                        CONSTRAINT FK_PaymentTransactions_Order FOREIGN KEY (order_id) REFERENCES dbo.Orders(id) ON DELETE CASCADE -- Xóa giao dịch nếu xóa đơn hàng
);
GO

CREATE NONCLUSTERED INDEX IX_PaymentTransactions_OrderId ON dbo.PaymentTransactions(order_id);
CREATE NONCLUSTERED INDEX IX_PaymentTransactions_GatewayId ON dbo.PaymentTransactions(gateway_transaction_id);
GO