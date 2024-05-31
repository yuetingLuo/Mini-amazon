import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { BootstrapVue, BootstrapVueIcons } from 'bootstrap-vue'
import App from './App.vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import ProductList from './views/ProductList.vue'
import OrderList from './views/OrderList.vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'

const routes = [
	{ path: "/list-product", component: ProductList },
	{ path: "/list-order", component: OrderList },
	{ path: "/login", component: Login },
	{ path: "/register", component: Register },
	{ path: "/", redirect: "/login" },

]

const router = createRouter({
	history: createWebHistory(),
	routes,
})

function isLoggedIn() {
	return localStorage.getItem('username') !== null;
}

localStorage.removeItem('username');
router.beforeEach((to, from, next) => {
    if (!isLoggedIn() && (to.path !== '/login' && to.path !== '/register')) {
        next('/login');
    } else if (isLoggedIn() && to.path === '/login') {
		next('/list-product');
	} else {
        next(); 
    }
})

createApp(App)
	.use(BootstrapVue)
	.use(BootstrapVueIcons)
	.use(router)
	.mount('#app')
