import {createRouter, createWebHistory } from "vue-router";
import type {RouteRecordRaw} from "vue-router";

import HomeView from "@/views/HomeView.vue";
import CatalogView from '@/views/CatalogView.vue';
import ProductView from '@/views/ProductView.vue';
import CartView from '@/views/CartView.vue';

const routes: RouteRecordRaw[] = [
    {
        path: "/",
        name: "home",
        component: () => HomeView,
        meta: {title: "Главная"}
    },
    {
        path: '/catalog',
        name: 'catalog',
        component: () => CatalogView
    },
    {
        path: '/product/:id',
        name: 'product',
        component: () => ProductView
    },
    {
        path: '/cart',
        name: 'cart',
        component: () => CartView
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(to, from, savedPosition) {
        return savedPosition || {top: 0}
    }
})

router.beforeEach((to) => {
    document.title = `${to.meta.title || 'Factura Shop'} | Factura Shop `
})

export default router