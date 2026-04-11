import { createRouter, createWebHistory } from "vue-router"
import type { RouteRecordRaw } from "vue-router"

const routes: RouteRecordRaw[] = [
    {
        path: "/",
        name: "home",
        component: () => import('@/views/HomeView.vue'),
        meta: { title: "Главная" }
    },
    {
        path: '/catalog',
        name: 'catalog',
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
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(_to, _from, savedPosition) {
        return savedPosition || { top: 0 }
    }
})

router.beforeEach((to, _from) => {
    document.title = `${to.meta.title || 'Factura Shop'} | Factura Shop`
})

export default router