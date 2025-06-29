// src/utils/formatters.js

/**
 * Định dạng số thành tiền tệ Việt Nam (VND).
 * @param {number | string | null | undefined} value Số tiền cần định dạng.
 * @returns {string} Chuỗi tiền tệ đã định dạng (e.g., "1.234.567 ₫") hoặc '0 ₫' nếu không hợp lệ.
 */
export const formatCurrency = (value) => {
  const number = Number(value);
  // Kiểm tra nếu giá trị không phải là số hợp lệ
  if (isNaN(number)) {
    return '0 ₫'; // Hoặc trả về chuỗi rỗng '' nếu muốn
  }
  // Sử dụng Intl.NumberFormat để định dạng tiền tệ theo chuẩn vi-VN
  return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(number);
};

/**
 * Định dạng đối tượng Date hoặc chuỗi ngày thành dạng ngày (DD/MM/YYYY).
 * @param {Date | string | null | undefined} dateInput Ngày cần định dạng.
 * @returns {string} Chuỗi ngày đã định dạng hoặc chuỗi rỗng nếu không hợp lệ.
 */
export const formatDate = (dateInput) => {
  if (!dateInput) return ''; // Trả về rỗng nếu input là null hoặc undefined
  try {
    const date = new Date(dateInput);
    // Kiểm tra xem đối tượng Date có hợp lệ không (Invalid Date)
    if (isNaN(date.getTime())) {
      return '';
    }
    // Sử dụng toLocaleDateString để định dạng ngày theo chuẩn vi-VN
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',   // Luôn hiển thị 2 chữ số cho ngày (01, 02, ..., 31)
      month: '2-digit', // Luôn hiển thị 2 chữ số cho tháng (01, 02, ..., 12)
      year: 'numeric'   // Hiển thị năm đầy đủ (e.g., 2023)
    });
  } catch (e) {
    console.error("Error formatting date:", dateInput, e); // Log lỗi để debug
    return ''; // Trả về rỗng nếu có lỗi xảy ra
  }
};

/**
 * Định dạng đối tượng Date hoặc chuỗi ngày thành dạng ngày giờ (DD/MM/YYYY HH:mm).
 * @param {Date | string | null | undefined} dateInput Ngày giờ cần định dạng.
 * @returns {string} Chuỗi ngày giờ đã định dạng hoặc chuỗi rỗng nếu không hợp lệ.
 */
export const formatDateTime = (dateInput) => {
  if (!dateInput) return '';
  try {
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return '';
    }
    // Sử dụng toLocaleString để định dạng cả ngày và giờ
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',  // Hiển thị giờ (00-23)
      minute: '2-digit' // Hiển thị phút (00-59)
      // second: '2-digit' // Bỏ comment nếu muốn hiển thị cả giây
    });
  } catch (e) {
    console.error("Error formatting date time:", dateInput, e);
    return '';
  }
};

/**
 * Định dạng thời gian tương đối (ví dụ: "vài giây trước", "Hôm qua", "2 ngày trước").
 * Thích hợp cho việc hiển thị thời gian của thông báo, bình luận,...
 * @param {Date | string | null | undefined} dateInput Ngày cần định dạng.
 * @returns {string} Chuỗi thời gian tương đối hoặc ngày đầy đủ (DD/MM/YYYY) nếu quá 7 ngày.
 */
export const formatDateRelative = (dateInput) => {
  if (!dateInput) return '';
  try {
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) return '';

    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    const diffInDays = Math.floor(diffInSeconds / (60 * 60 * 24));

    // Nếu là ngày trong tương lai, hiển thị ngày cụ thể
    if (diffInSeconds < 0) return formatDate(date);

    // Cùng ngày hôm nay
    if (diffInDays === 0) {
      if (diffInSeconds < 60) return `vài giây trước`;
      const diffInMinutes = Math.floor(diffInSeconds / 60);
      if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
      const diffInHours = Math.floor(diffInSeconds / (60 * 60));
      return `${diffInHours} giờ trước`;
    }

    // Ngày hôm qua
    if (diffInDays === 1) return 'Hôm qua';

    // Trong vòng 1 tuần
    if (diffInDays < 7) return `${diffInDays} ngày trước`;

    // Nếu hơn 7 ngày, hiển thị ngày cụ thể
    return formatDate(date);
  } catch (e) {
    console.error("Error formatting relative date:", dateInput, e);
    return '';
  }
};


/**
 * Trả về tên hiển thị tiếng Việt cho trạng thái đơn hàng.
 * @param {string | null | undefined} status Mã trạng thái (e.g., 'PENDING').
 * @returns {string} Tên trạng thái tiếng Việt hoặc chính mã status nếu không tìm thấy.
 */
export const formatStatus = (status) => {
  // <<< THÊM KIỂM TRA Ở ĐÂY >>>
  if (typeof status !== 'string' || !status) {
    return 'Không rõ'; // Trả về giá trị mặc định nếu không phải string hoặc rỗng/null/undefined
  }
  // ==========================

  const statusMap = {
    'PENDING': 'Chờ xác nhận',
    'PROCESSING': 'Đang xử lý',
    'SHIPPING': 'Đang giao hàng',
    'COMPLETED': 'Đã hoàn thành',
    'CANCELLED': 'Đã hủy',
    'PAYMENT_PENDING': 'Chờ thanh toán',
    // Thêm các trạng thái khác nếu có
  };
  // Giờ status chắc chắn là string nên có thể gọi toUpperCase() an toàn
  return statusMap[status.toUpperCase()] || status; // Trả về status gốc nếu không có trong map
};

/**
 * Trả về class CSS (Bootstrap badge) tương ứng với trạng thái đơn hàng.
 * @param {string | null | undefined} status Mã trạng thái.
 * @returns {string} Tên class CSS (e.g., 'text-bg-success').
 */
export const getStatusClass = (status) => {
  if (!status) return 'text-bg-light text-dark border'; // Thêm border cho màu sáng
  const classMap = {
    'PENDING': 'text-bg-secondary',
    'PROCESSING': 'text-bg-info',
    'SHIPPING': 'text-bg-primary',
    'COMPLETED': 'text-bg-success',
    'CANCELLED': 'text-bg-danger',
    'PAYMENT_PENDING': 'text-bg-warning text-dark', // text-dark cho nền vàng dễ đọc hơn
    'DELIVERED': 'text-bg-success bg-opacity-75', // Biến thể của success
    'RETURNED': 'text-bg-dark', // Màu tối cho hoàn trả
  };
  // Trả về class mặc định nếu status không khớp
  return classMap[status.toUpperCase()] || 'text-bg-light text-dark border';
};

/**
 * Danh sách tất cả các trạng thái có thể có (dùng cho dropdown lọc hoặc form admin).
 * Có thể tạo object với key là mã, value là tên tiếng Việt để dùng tiện hơn.
 */
export const allStatuses = [
  'PENDING',
  'PROCESSING',
  'SHIPPING',
  'COMPLETED',
  'CANCELLED',
  'PAYMENT_PENDING'
];

/**
 * Định nghĩa các bước chuyển trạng thái hợp lệ (Ví dụ).
 * Key: Trạng thái hiện tại (viết hoa).
 * Value: Mảng các trạng thái tiếp theo có thể chuyển đến (viết hoa).
 * Dùng trong logic nghiệp vụ ở backend hoặc frontend (ví dụ: disable các option không hợp lệ).
 */
export const validTransitions = {
  'PENDING': ['PROCESSING', 'CANCELLED'],
  'PAYMENT_PENDING': ['PROCESSING', 'CANCELLED'],
  'PROCESSING': ['SHIPPING', 'CANCELLED'],
  'SHIPPING': ['DELIVERED', 'CANCELLED'], // Đổi COMPLETED thành DELIVERED nếu cần
  'DELIVERED': ['COMPLETED', 'RETURNED'], // Có thể chuyển sang Hoàn thành (sau khi đối soát?) hoặc Hoàn trả
  'COMPLETED': [], // Không chuyển từ trạng thái cuối cùng
  'CANCELLED': [], // Không chuyển từ trạng thái cuối cùng
  'RETURNED': [], // Không chuyển từ trạng thái cuối cùng
};

/**
 * Chuyển đổi mã bậc khách hàng sang tên tiếng Việt.
 * @param {string | null | undefined} tier Mã bậc (e.g., 'BRONZE', 'SILVER').
 * @returns {string} Tên bậc tiếng Việt hoặc chính mã tier nếu không khớp.
 */
export const formatTier = (tier) => {
  if (!tier) return 'Chưa xếp hạng'; // Hoặc 'Tất cả' tùy ngữ cảnh
  const tierMap = {
    'BRONZE': 'Đồng',
    'SILVER': 'Bạc',
    'GOLD': 'Vàng',
    'PLATINUM': 'Bạch Kim', // Ví dụ thêm bậc mới
    'DIAMOND': 'Kim Cương'
  };
  // Đảm bảo so sánh không phân biệt hoa thường và trả về mã gốc nếu không tìm thấy
  return tierMap[tier.toUpperCase()] || tier;
};

/**
 * Trả về class CSS (Bootstrap badge) tương ứng với bậc khách hàng.
 * @param {string | null | undefined} tier Mã bậc (e.g., 'BRONZE', 'SILVER').
 * @returns {string} Tên class CSS (e.g., 'bg-success').
 */
export const getTierClass = (tier) => {
  if (!tier) return 'bg-light text-dark border'; // Default class cho chưa xếp hạng
  const tierMap = {
    'BRONZE': 'bg-bronze text-white',   // Định nghĩa màu bronze trong CSS hoặc dùng màu gần đúng
    'SILVER': 'bg-silver text-dark',    // Định nghĩa màu silver trong CSS hoặc dùng màu xám nhạt
    'GOLD': 'bg-warning text-dark',     // Dùng màu vàng warning
    'PLATINUM': 'bg-platinum text-dark',// Định nghĩa màu platinum hoặc dùng màu sáng khác
    'DIAMOND': 'bg-diamond text-white'  // Định nghĩa màu diamond hoặc dùng màu primary/info
    // Ví dụ dùng màu Bootstrap có sẵn:
    // 'BRONZE': 'bg-secondary bg-opacity-75 text-dark',
    // 'SILVER': 'bg-info bg-opacity-75 text-dark',
    // 'PLATINUM': 'bg-light text-primary border border-primary',
    // 'DIAMOND': 'bg-primary text-white'
  };
  // Đảm bảo so sánh không phân biệt hoa thường
  return tierMap[tier.toUpperCase()] || 'bg-light text-dark border'; // Trả về default nếu không khớp
};

/**
 * Danh sách các bậc khách hàng có thể có.
 */
export const allTiers = [
  {code: 'BRONZE', name: 'Đồng'},
  {code: 'SILVER', name: 'Bạc'},
  {code: 'GOLD', name: 'Vàng'},
  {code: 'PLATINUM', name: 'Bạch Kim'},
  {code: 'DIAMOND', name: 'Kim Cương'}
];


// Thêm các hàm format hoặc helper khác nếu cần
// Ví dụ: format số điện thoại, truncate text,...
