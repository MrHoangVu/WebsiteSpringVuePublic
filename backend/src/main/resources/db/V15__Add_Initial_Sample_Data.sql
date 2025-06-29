-- File: Vxx__Add_Initial_Sample_Data.sql
-- Script tạo dữ liệu mẫu ban đầu cho các bảng

PRINT 'Starting data insertion...';

-- 1. Thêm Users
PRINT 'Inserting sample users...';
INSERT INTO dbo.Users (username, password, full_name, role, created_at) VALUES
                                                                            (N'admin', '$2a$10$pUQX7MsCQK/YhNtv5QbKVeRAfF3JiLfeP.yYOnwogHKDTJQe4LfLu', N'Administrator', N'ADMIN', GETDATE()), -- Mật khẩu là 'password' đã hash
                                                                            (N'customer1', '$2a$10$pUQX7MsCQK/YhNtv5QbKVeRAfF3JiLfeP.yYOnwogHKDTJQe4LfLu', N'Nguyễn Văn A', N'CUSTOMER', GETDATE()), -- Mật khẩu là 'password' đã hash
                                                                            (N'customer2', '$2a$10$pUQX7MsCQK/YhNtv5QbKVeRAfF3JiLfeP.yYOnwogHKDTJQe4LfLu', N'Trần Thị B', N'CUSTOMER', GETDATE()); -- Mật khẩu là 'password' đã hash

-- 2. Thêm Categories
PRINT 'Inserting sample categories...';
INSERT INTO dbo.Categories (name, slug, description, created_at, updated_at) VALUES
                                                                                 (N'Tượng Di Lặc', N'tuong-di-lac', N'Tượng Phật Di Lặc mang ý nghĩa vui vẻ, hạnh phúc và tài lộc.', GETDATE(), GETDATE()),
                                                                                 (N'Tượng Quan Công', N'tuong-quan-cong', N'Tượng Quan Công (Quan Vũ) tượng trưng cho sự trung thành, dũng cảm và bảo vệ.', GETDATE(), GETDATE()),
                                                                                 (N'Tượng Phật Bà Quan Âm', N'tuong-phat-ba-quan-am', N'Tượng Phật Bà Quan Âm biểu tượng cho lòng từ bi, cứu khổ cứu nạn.', GETDATE(), GETDATE()),
                                                                                 (N'Tỳ Hưu Phong Thủy', N'ty-huu-phong-thuy', N'Tỳ Hưu là linh vật chiêu tài, giữ lộc và hóa giải sát khí.', GETDATE(), GETDATE()),
                                                                                 (N'Thiềm Thừ (Cóc Ngậm Tiền)', N'thiem-thu-coc-ngam-tien', N'Thiềm Thừ mang lại may mắn về tiền bạc, sự thịnh vượng.', GETDATE(), GETDATE());

-- Khai báo và lấy ID Categories và Users để sử dụng trong các bước sau
PRINT 'Declaring and fetching IDs for subsequent inserts...';
DECLARE @CatDiLacId INT, @CatQuanCongId INT, @CatQuanAmId INT, @CatTyHuuId INT, @CatThiemThuId INT;
DECLARE @AdminUserId INT, @Customer1UserId INT, @Customer2UserId INT;

SELECT @CatDiLacId = id FROM dbo.Categories WHERE slug = N'tuong-di-lac';
SELECT @CatQuanCongId = id FROM dbo.Categories WHERE slug = N'tuong-quan-cong';
SELECT @CatQuanAmId = id FROM dbo.Categories WHERE slug = N'tuong-phat-ba-quan-am';
SELECT @CatTyHuuId = id FROM dbo.Categories WHERE slug = N'ty-huu-phong-thuy';
SELECT @CatThiemThuId = id FROM dbo.Categories WHERE slug = N'thiem-thu-coc-ngam-tien';

SELECT @AdminUserId = id FROM dbo.Users WHERE username = N'admin';
SELECT @Customer1UserId = id FROM dbo.Users WHERE username = N'customer1';
SELECT @Customer2UserId = id FROM dbo.Users WHERE username = N'customer2';

PRINT 'Category and User IDs fetched successfully.';

-- 3. Thêm Products
PRINT 'Inserting sample products...';
INSERT INTO dbo.Products (name, slug, description, price, stock, image_url, dimensions, material, category_id, is_active, created_at, updated_at) VALUES
                                                                                                                                                      (N'Tượng Di Lặc Cưỡi Cá Chép Gỗ Hương', N'tuong-di-lac-cuoi-ca-chep-go-huong', N'<p>Tượng Phật Di Lặc ngồi trên cá chép bằng <strong>gỗ hương đỏ</strong> tự nhiên.</p><ul><li>Kích thước: Cao 40cm x Rộng 30cm x Sâu 20cm</li><li>Ý nghĩa: Mang lại may mắn, tài lộc và thành công trong sự nghiệp (cá chép vượt vũ môn).</li></ul>', 5500000, 8, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'40x30x20 cm', N'Gỗ Hương Đỏ', @CatDiLacId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Tượng Di Lặc Vác Cành Đào Gỗ Mun', N'tuong-di-lac-vac-canh-dao-go-mun', N'<p>Phật Di Lặc vui tươi vác cành đào tiên, chế tác từ <em>gỗ mun sừng</em> quý hiếm.</p><p>Biểu tượng cho sức khỏe, trường thọ và hạnh phúc viên mãn.</p>', 12800000, 3, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Cao 60cm', N'Gỗ Mun Sừng', @CatDiLacId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Tượng Quan Công Cưỡi Ngựa Gỗ Trắc Đỏ Đen', N'tuong-quan-cong-cuoi-ngua-go-trac', N'Tượng Quan Công oai phong cưỡi ngựa Xích Thố, làm từ gỗ trắc đỏ đen liền khối.<br>Thể hiện khí phách anh hùng, trấn trạch, trừ tà.', 18000000, 2, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Cao 70cm', N'Gỗ Trắc Đỏ Đen', @CatQuanCongId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Tượng Phật Bà Quan Âm Ngồi Đài Sen Gỗ Pơ Mu', N'tuong-phat-ba-quan-am-pomu', N'Tượng Quan Thế Âm Bồ Tát ngồi thiền trên đài sen, gỗ Pơ Mu thơm nhẹ.<br>Mang lại bình an, giải trừ phiền não.', 6200000, 10, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Cao 50cm', N'Gỗ Pơ Mu', @CatQuanAmId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Cặp Tỳ Hưu Xanh Ngọc Pakistan Lớn', N'cap-ty-huu-xanh-ngoc-pakistan-lon', N'Cặp Tỳ Hưu chế tác từ đá xanh ngọc Pakistan tự nhiên, vân đẹp.<br>Linh vật số 1 về chiêu tài, hút lộc, đặc biệt tốt cho kinh doanh.', 4500000, 15, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Dài 20cm', N'Đá Ngọc Pakistan', @CatTyHuuId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Thiềm Thừ Gỗ Nu Nghiến Độc Đáo', N'thiem-thu-go-nu-nghien', N'Cóc 3 chân ngậm tiền vàng, chế tác từ gỗ nu nghiến với vân hoa độc đáo.<br>Đặt ở bàn thờ Thần Tài, quầy thu ngân để hút tài lộc.', 3900000, 7, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Ngang 25cm', N'Gỗ Nu Nghiến', @CatThiemThuId, 1, GETDATE(), GETDATE()),
                                                                                                                                                      (N'Tượng Di Lặc Gỗ Thông (Hết Hàng)', N'tuong-di-lac-go-thong-het-hang', N'Mẫu tượng Di Lặc giá rẻ bằng gỗ thông, đã ngừng kinh doanh.', 800000, 0, N'https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg', N'Cao 30cm', N'Gỗ Thông', @CatDiLacId, 0, GETDATE(), GETDATE()); -- Sản phẩm inactive (is_active = 0, stock = 0)

-- 4. Thêm Promotions
PRINT 'Inserting sample promotions...';
INSERT INTO dbo.Promotions (code, name, description, discount_type, discount_value, start_date, end_date, max_usage, min_order_value, is_active, created_at, updated_at) VALUES
                                                                                                                                                                             (N'GIAM10', N'Giảm giá 10% đơn hàng đầu tiên', N'Giảm 10% cho tổng giá trị đơn hàng đầu tiên trên 500.000đ', N'PERCENTAGE', 10.00, DATEADD(day, -1, GETDATE()), DATEADD(month, 3, GETDATE()), NULL, 500000.00, 1, GETDATE(), GETDATE()), -- KM đang hoạt động, không giới hạn lượt dùng
                                                                                                                                                                             (N'KM200K', N'Giảm 200k đơn hàng trên 2 triệu', N'Giảm trực tiếp 200.000đ cho đơn hàng từ 2.000.000đ', N'FIXED_AMOUNT', 200000.00, DATEADD(month, -2, GETDATE()), DATEADD(day, -1, GETDATE()), 100, 2000000.00, 0, GETDATE(), GETDATE()), -- KM đã hết hạn, is_active = 0
                                                                                                                                                                             (N'NOEL2024', N'Khuyến mãi Giáng Sinh', N'Giảm 15% toàn bộ sản phẩm', N'PERCENTAGE', 15.00, '2024-12-15 00:00:00', '2024-12-25 23:59:59', NULL, NULL, 0, GETDATE(), GETDATE()); -- KM chưa tới ngày, is_active = 0

-- 5. Thêm Articles (bài viết)
PRINT 'Inserting sample articles...';
INSERT INTO dbo.Articles (user_id, title, slug, content, excerpt, is_published, published_at, created_at, updated_at) VALUES
                                                                                                                          (@AdminUserId, -- Sử dụng ID của user admin đã lấy ở trên
                                                                                                                           N'Cách Bài Trí Tượng Di Lặc Để Hút Tài Lộc Tối Đa',
                                                                                                                           N'cach-bai-tri-tuong-di-lac-de-hut-tai-loc',
                                                                                                                           N'<h2>Đặt Tượng Di Lặc Ở Đâu?</h2><p>Vị trí tốt nhất để đặt tượng Phật Di Lặc là ở cung Đông Nam của phòng khách, phòng làm việc hoặc toàn bộ ngôi nhà. Đây là cung tài lộc theo phong thủy.</p><h3>Lưu ý quan trọng:</h3><ul><li>Không đặt tượng trực tiếp xuống sàn nhà.</li><li>Đặt tượng ở nơi sạch sẽ, trang trọng.</li><li>Luôn giữ cho tượng sạch sẽ.</li></ul><p>Chúc bạn và gia đình luôn gặp nhiều may mắn!</p>',
                                                                                                                           N'Hướng dẫn chi tiết cách đặt tượng Phật Di Lặc sao cho hợp phong thủy, mang lại nhiều may mắn và tài lộc cho gia chủ.',
                                                                                                                           1, -- is_published = true
                                                                                                                           GETDATE(), -- published_at (đặt là ngày hiện tại khi publish)
                                                                                                                           GETDATE(), GETDATE()),
                                                                                                                          (@AdminUserId, -- Sử dụng ID của user admin
                                                                                                                           N'Tìm Hiểu Ý Nghĩa Của Tượng Quan Công Trong Phong Thủy',
                                                                                                                           N'y-nghia-tuong-quan-cong-trong-phong-thuy',
                                                                                                                           N'<h1>Ý Nghĩa Phong Thủy Của Tượng Quan Công</h1><p>Tượng Quan Công không chỉ là vật trang trí mà còn mang nhiều ý nghĩa sâu sắc trong phong thủy...</p> <!-- Nội dung đầy đủ hơn cần được thêm vào -->',
                                                                                                                           N'Quan Công là biểu tượng của sự trung thành, dũng cảm và bảo vệ. Tìm hiểu ý nghĩa khi đặt tượng Quan Công trong nhà.',
                                                                                                                           0, -- is_published = false (đây là bản nháp)
                                                                                                                           NULL, -- published_at = NULL vì chưa publish
                                                                                                                           GETDATE(), GETDATE());

PRINT 'Finished inserting initial sample data.';
GO -- GO ở cuối cùng để đảm bảo lô lệnh cuối được thực thi