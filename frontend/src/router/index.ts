import {createRouter, createWebHistory } from "vue-router";
import type {RouteRecordRaw} from "vue-router";

import HomeView from "@/views/HomeView.vue";

const routes: RouteRecordRaw[] = [
    {
        path: "/",
        name: "home",
        component: () => HomeView,
        meta: {title: "Главная"}
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