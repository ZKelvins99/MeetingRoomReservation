import axios from 'axios'
import store from '../store'
import { Message } from 'element-ui'
import router from '../router'

// 创建axios实例
const api = axios.create({
    baseURL: process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080/api',
    timeout: 30000
})

// 请求拦截器
api.interceptors.request.use(
    config => {
        const token = store.getters['auth/token']
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
api.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res
        }
        Message.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        console.error('响应错误:', error)

        let message = '网络错误'
        if (error.response) {
            const { status } = error.response
            switch (status) {
                case 401:
                    message = '登录已过期，请重新登录'
                    store.dispatch('auth/logout')
                    router.push('/login')
                    break
                case 403:
                    message = '权限不足'
                    break
                case 404:
                    message = '请求的资源不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = `请求失败 (${status})`
            }
        }

        Message.error(message)
        return Promise.reject(error)
    }
)

// API接口定义
export default {
    // 认证相关
    auth: {
        login: (data) => api.post('/auth/login', data),
        logout: () => api.post('/auth/logout'),
        getUserInfo: () => api.get('/auth/info')
    },

    // 会议室相关
    rooms: {
        getAll: () => api.get('/rooms/list'),
        getById: (id) => api.get(`/rooms/${id}`),
        getAvailable: (params) => api.get('/rooms/available', { params }),
        add: (data) => api.post('/rooms/add', data),
        update: (data) => api.put('/rooms/update', data),
        delete: (id) => api.delete(`/rooms/${id}`)
    },

    // 预订相关
    bookings: {
        book: (data) => api.post('/bookings/book', data),
        getMy: (params) => api.get('/bookings/my', { params }),
        getAll: (params) => api.get('/bookings/all', { params }),
        getById: (id) => api.get(`/bookings/${id}`),
        cancel: (id) => api.put(`/bookings/${id}/cancel`),
        getQRCode: (id) => api.get(`/bookings/${id}/qrcode`)
    },

    // 签到相关
    checkin: {
        scan: (data) => api.post('/checkin/scan', data),
        manual: (data) => api.post('/checkin/manual', data),
        getList: (params) => api.get('/checkin/list', { params }),
        getStatistics: (bookingId) => api.get(`/checkin/statistics/${bookingId}`)
    }
}