// src/router/index.js
import {createRouter, createWebHistory} from 'vue-router';
import {useAuthStore} from '@/store/auth';

// Import Views (Người dùng & Chung)
import HomeView from '../views/public/HomeView.vue';
import ProductListView from '../views/public/ProductListView.vue';
import ProductDetailView from '../views/public/ProductDetailView.vue';
import ContactView from '../views/public/ContactView.vue';
import LoginView from '../views/auth/LoginView.vue';
import RegisterView from '../views/auth/RegisterView.vue';
import ArticleListView from '../views/public/ArticleListView.vue';
import ArticleDetailView from '../views/public/ArticleDetailView.vue';
import CreateArticleView from '../views/user/CreateArticleView.vue';
import ShoppingCartView from '../views/public/ShoppingCartView.vue';
import CheckoutView from '../views/public/CheckoutView.vue';
import OrderSuccessView from '../views/public/OrderSuccessView.vue';
import NotFoundView from '../views/public/NotFoundView.vue';
import UserProfileView from "@/views/user/UserProfileView.vue";
import UserOrderDetailView from '../views/user/UserOrderDetailView.vue';
import OrderHistoryView from '../views/user/OrderHistoryView.vue';
// <<< IMPORT VIEWS BÀI VIẾT MỚI >>>
import AdminArticleListView from '../views/admin/AdminArticleListView.vue';
import AdminArticleFormView from '../views/admin/AdminArticleFormView.vue';

// Import Admin Views
import AdminDashboardView from '../views/admin/AdminDashboardView.vue';
import AdminOrderListView from '../views/admin/AdminOrderListView.vue';
import AdminOrderDetailView from '../views/admin/AdminOrderDetailView.vue';
import AdminProductListView from '../views/admin/AdminProductListView.vue';
import AdminProductFormView from '../views/admin/AdminProductFormView.vue';
import AdminUserListView from '../views/admin/AdminUserListView.vue';
import AdminUserDetailView from '../views/admin/AdminUserDetailView.vue';
import AdminShippingMethodListView from '../views/admin/AdminShippingMethodListView.vue';
import AdminShippingMethodFormView from '../views/admin/AdminShippingMethodFormView.vue';
import AdminPromotionListView from '../views/admin/AdminPromotionListView.vue'; // Import view thật
import AdminPromotionFormView from '../views/admin/AdminPromotionFormView.vue'; // Import view thật
// Import Layouts
import AdminLayout from '../components/layout/AdminLayout.vue';

const routes = [
  // --- Các Route Công khai (Public User Routes) ---
  {path: '/', name: 'home', component: HomeView},
  {path: '/products', name: 'productList', component: ProductListView},
  {
    path: '/category/:categorySlug',
    name: 'productListByCategory',
    component: ProductListView,
    props: true
  },
  {path: '/product/:slug', name: 'productDetail', component: ProductDetailView, props: true},
  {path: '/contact', name: 'contact', component: ContactView},
  {path: '/articles', name: 'articleList', component: ArticleListView},
  {path: '/article/:slug', name: 'articleDetail', component: ArticleDetailView, props: true},
  {path: '/cart', name: 'shoppingCart', component: ShoppingCartView},

  // --- Các Route Chỉ Dành Cho Khách (Guest Only Routes) ---
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: {requiresGuest: true}
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {requiresGuest: true}
  },

  // --- Các Route Yêu Cầu Đăng Nhập (Authenticated User Routes) ---
  {
    path: '/articles/create',
    name: 'createArticle',
    component: CreateArticleView,
    meta: {requiresAuth: true}
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: CheckoutView,
    meta: {requiresAuth: true}
  },
  {
    path: '/order-success/:orderId',
    name: 'orderSuccess',
    component: OrderSuccessView,
    props: true,
    meta: {requiresAuth: true}
  },
  {
    path: '/my-profile',
    name: 'userProfile',
    component: UserProfileView,
    meta: {requiresAuth: true}
  },
  {
    path: '/my-orders',
    name: 'orderHistory',
    component: OrderHistoryView,
    meta: {requiresAuth: true}
  },
  {
    path: '/my-orders/:orderId',
    name: 'userOrderDetail',
    component: UserOrderDetailView,
    props: true,
    meta: {requiresAuth: true}
  },

  // === CÁC ROUTE ADMIN ===
  {
    path: '/admin',
    component: AdminLayout,
    meta: {requiresAuth: true, requiresAdmin: true},
    children: [
      // Redirect mặc định đến dashboard
      {path: '', redirect: {name: 'adminDashboard'}},
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: AdminDashboardView,
      },
      // Quản lý Đơn hàng
      {
        path: 'orders',
        name: 'adminOrderList',
        component: AdminOrderListView,
      },
      {
        path: 'orders/:orderId',
        name: 'adminOrderDetail',
        component: AdminOrderDetailView,
        props: true,
      },
      // Quản lý Sản phẩm
      {
        path: 'products',
        name: 'adminProductList',
        component: AdminProductListView,
      },
      {
        path: 'products/new',
        name: 'adminProductNew',
        component: AdminProductFormView,
      },
      {
        path: 'products/:productId/edit',
        name: 'adminProductEdit',
        component: AdminProductFormView,
        props: true,
      },
      // Quản lý Người dùng (User)
      {
        path: 'users',
        name: 'adminUserList',
        component: AdminUserListView,
      },
      {
        path: 'users/:userId',
        name: 'adminUserDetail',
        component: AdminUserDetailView,
        props: true,
      },
      // Quản lý Phương thức vận chuyển (Shipping Methods)
      {
        path: 'shipping-methods', // -> /admin/shipping-methods
        name: 'adminShippingMethodList',
        component: AdminShippingMethodListView, // Sử dụng component list PTVC
        // meta không cần thiết vì đã kế thừa từ cha
      },
      {
        path: 'shipping-methods/new', // -> /admin/shipping-methods/new
        name: 'adminShippingMethodNew',
        component: AdminShippingMethodFormView, // Sử dụng component form PTVC
      },
      {
        path: 'shipping-methods/:id/edit', // -> /admin/shipping-methods/1/edit
        name: 'adminShippingMethodEdit',
        component: AdminShippingMethodFormView, // Sử dụng component form PTVC
        props: true, // Truyền id làm prop
      },
      // Quản lý Khuyến mãi (Promotions) - Placeholder
      {
        path: 'promotions', // -> /admin/promotions
        name: 'adminPromotionList',
        component: AdminPromotionListView, // <<< SỬA COMPONENT
        meta: {requiresAuth: true, requiresAdmin: true}
      },
      {
        path: 'promotions/new', // -> /admin/promotions/new
        name: 'adminPromotionNew',
        component: AdminPromotionFormView, // <<< SỬA COMPONENT
        meta: {requiresAuth: true, requiresAdmin: true}
      },
      {
        path: 'promotions/:id/edit', // -> /admin/promotions/1/edit
        name: 'adminPromotionEdit',
        component: AdminPromotionFormView, // <<< SỬA COMPONENT
        props: true, // Truyền id làm prop
        meta: {requiresAuth: true, requiresAdmin: true}
      },

      // <<< THÊM ROUTES QUẢN LÝ BÀI VIẾT >>>
      {
        path: 'articles', // -> /admin/articles
        name: 'adminArticleList',
        component: AdminArticleListView,
        meta: {requiresAuth: true, requiresAdmin: true}
      },
      {
        path: 'articles/new', // -> /admin/articles/new
        name: 'adminArticleNew',
        component: AdminArticleFormView,
        meta: {requiresAuth: true, requiresAdmin: true}
      },
      {
        path: 'articles/:id/edit', // -> /admin/articles/123/edit (Lưu ý: ID bài viết là Long nên param có thể dài)
        name: 'adminArticleEdit',
        component: AdminArticleFormView,
        props: true, // Truyền id làm prop
        meta: {requiresAuth: true, requiresAdmin: true}
      },
      // Thêm các route admin khác vào đây
    ]
  },

  // --- Route Không Tìm Thấy (Must be the last route) ---
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('../views/public/NotFoundView.vue') // Lazy loading
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || {top: 0};
  }
});

// --- Global Navigation Guard ---
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);
  const requiresGuest = to.matched.some(record => record.meta.requiresGuest);

  // 1. Yêu cầu đăng nhập
  if (requiresAuth && !authStore.isAuthenticated) {
    // console.log(`Route Guard: "${to.path}" requires Auth. Redirecting to login.`);
    authStore.setReturnUrl(to.fullPath);
    return next({name: 'login'});
  }

  // 2. Yêu cầu quyền Admin
  if (requiresAdmin && !authStore.isAdmin) {
    if (!authStore.isAuthenticated) {
      // console.log(`Route Guard: "${to.path}" requires Admin. Not logged in. Redirecting to login.`);
      authStore.setReturnUrl(to.fullPath);
      return next({name: 'login'});
    } else {
      // console.warn(`Route Guard: "${to.path}" requires Admin. Not Admin. Redirecting to home.`);
      return next({name: 'home'});
    }
  }

  // 3. Yêu cầu là Khách (chưa đăng nhập)
  if (requiresGuest && authStore.isAuthenticated) {
    // console.log(`Route Guard: "${to.path}" requires Guest. Logged in. Redirecting.`);
    return next({name: authStore.isAdmin ? 'adminDashboard' : 'home'});
  }

  // Cho phép đi tiếp
  next();
});

export default router;
