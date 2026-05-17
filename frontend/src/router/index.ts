import { createRouter, createWebHistory } from "vue-router"
import type { RouteRecordRaw } from "vue-router"
// lazy‑load AdminDashboard to avoid TS import error
import { useAuthStore } from '@/stores/auth'


const routes: RouteRecordRaw[] = [
    {
        path: "/",
        name: "home",
        component: () => import('@/views/HomeView.vue'),
        meta: { title: "Главная" }
    },
    {
        path: '/categories',
        name: 'categories',
        component: () => import('@/views/CategoriesView.vue'),
        meta: { title: "Все категории" }
    },
    {
        path: '/catalog',
        name: 'catalog',
        component: () => import('@/views/CatalogView.vue'),
        meta: { title: "Каталог" }
    },
    {
        path: '/catalog/:categorySlug',
        name: 'catalog-category',
        component: () => import('@/views/CatalogView.vue'),
        meta: { title: "Каталог" }
    },
    {
        path: '/catalog/:categorySlug/:subcategorySlug',
        name: 'catalog-subcategory',
        component: () => import('@/views/CatalogView.vue'),
        meta: { title: "Каталог" }
    },
    {
        path: '/product/:id',
        name: 'product',
        component: () => import('@/views/ProductView.vue'),
        meta: { title: "Товар" }
    },
    {
        path: '/cart',
        name: 'cart',
        component: () => import('@/views/CartView.vue'),
        meta: { title: "Корзина", requiresAuth: true }
    },
    {
        path: '/search',
        name: 'search',
        component: () => import('@/views/SearchView.vue'),
        meta: { title: "Поиск" }
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/LoginView.vue'),
        meta: { title: "Вход" }
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/RegisterView.vue'),
        meta: { title: "Регистрация" }
    },
    {
        path: '/verify',
        name: 'verify',
        component: () => import('@/views/VerifyView.vue'),
        meta: { title: "Подтверждение email" }
    },
    {
        path: '/profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: "Профиль", requiresAuth: true }
    },
    {
        path: '/admin',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { requiresAdmin: true },
    },
    {
        path: '/payment',
        name: 'payment',
        component: () => import('@/views/PaymentView.vue'),
        meta: { title: "Способы оплаты" }
    },
    {
        path: '/delivery',
        name: 'delivery',
        component: () => import('@/views/DeliveryView.vue'),
        meta: { title: "Доставка" }
    },
    {
        path: '/privacy',
        name: 'privacy',
        component: () => import('@/views/PrivacyView.vue'),
        meta: { title: "Политика конфиденциальности" }
    },
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(_to, _from, savedPosition) {
        return savedPosition || { top: 0 }
    }
})

router.beforeEach((to, _from, next) => {
    const store = useAuthStore()

    if (to.meta.requiresAdmin) {
        if (!store.isAuthenticated) return next({ name: 'login', query: { redirect: to.fullPath } })
        if (store.currentUser?.role !== 'ADMIN') return next('/')
        return next()
    }

    if (to.meta.requiresAuth && !store.isAuthenticated) {
        return next({ name: 'login', query: { redirect: to.fullPath } })
    }

    next()
})
export default router