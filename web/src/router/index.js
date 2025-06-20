import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'

Vue.use(VueRouter)

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { requiresAuth: false, title: '登录' }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('../components/Layout.vue'),
        redirect: '/dashboard',
        meta: { requiresAuth: true },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue'),
                meta: { title: '首页', icon: 'el-icon-s-home' }
            },
            {
                path: 'booking',
                name: 'Booking',
                component: () => import('../views/BookingManage.vue'),
                meta: { title: '预订会议室', icon: 'el-icon-calendar' }
            },
            {
                path: 'my-bookings',
                name: 'MyBookings',
                component: () => import('../views/MyBookings.vue'),
                meta: { title: '我的预订', icon: 'el-icon-tickets' }
            },
            {
                path: 'checkin',
                name: 'Checkin',
                component: () => import('../views/CheckinManage.vue'),
                meta: { title: '签到管理', icon: 'el-icon-location-information' }
            },
            {
                path: 'rooms',
                name: 'Rooms',
                component: () => import('../views/MeetingRooms.vue'),
                meta: {
                    title: '会议室管理',
                    icon: 'el-icon-office-building',
                    requiresAdmin: true
                }
            },
            {
                path: 'statistics',
                name: 'Statistics',
                component: () => import('../views/Statistics.vue'),
                meta: {
                    title: '统计报表',
                    icon: 'el-icon-data-analysis',
                    requiresAdmin: true
                }
            }
        ]
    },
    {
        path: '/checkin-scan',
        name: 'CheckinScan',
        component: () => import('../views/CheckinScan.vue'),
        meta: { requiresAuth: false, title: '扫码签到' }
    },
    {
        path: '*',
        redirect: '/'
    }
]

const router = new VueRouter({
    mode: 'hash',
    base: process.env.BASE_URL,
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = store.getters['auth/token']
    const userInfo = store.getters['auth/userInfo']

    // 设置页面标题
    if (to.meta.title) {
        document.title = `${to.meta.title} - 会议室预约签到系统`
    }

    // 检查是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!token) {
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
            return
        }

        // 检查管理员权限
        if (to.meta.requiresAdmin && userInfo?.role !== 'ADMIN') {
            Vue.prototype.$message.error('权限不足')
            next('/')
            return
        }
    }

    // 已登录用户访问登录页，重定向到首页
    if (to.path === '/login' && token) {
        next(from.query?.redirect || '/')
        return
    }

    next()
})

export default router