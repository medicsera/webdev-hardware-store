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
        meta: { title: "Корзина" }
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
        path: '/profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: "Профиль" }
    },
    {
        path: '/admin',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { requiresAdmin: true },
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
    if (to.meta.requiresAdmin && store.currentUser?.role !== 'ADMIN') {
        // non‑admin users are sent back to home
        return next('/')
    }
    next()
})
export default router