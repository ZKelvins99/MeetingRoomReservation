import Cookies from 'js-cookie'
import api from '../../api'

const TOKEN_KEY = 'meeting_token'

const state = {
    token: Cookies.get(TOKEN_KEY) || '',
    userInfo: null
}

const getters = {
    token: state => state.token,
    userInfo: state => state.userInfo,
    isAdmin: state => state.userInfo?.role === 'ADMIN',
    isLoggedIn: state => !!state.token,
    userName: state => state.userInfo?.realName || state.userInfo?.username || '用户'
}

const mutations = {
    SET_TOKEN(state, token) {
        state.token = token
        if (token) {
            Cookies.set(TOKEN_KEY, token, { expires: 7 })
        } else {
            Cookies.remove(TOKEN_KEY)
        }
    },

    SET_USER_INFO(state, userInfo) {
        state.userInfo = userInfo
    },

    CLEAR_AUTH(state) {
        state.token = ''
        state.userInfo = null
        Cookies.remove(TOKEN_KEY)
    }
}

const actions = {
    async login({ commit }, loginForm) {
        try {
            // 调用真实的后端 API
            const response = await api.auth.login(loginForm)
            const { token, user } = response.data

            commit('SET_TOKEN', token)
            commit('SET_USER_INFO', user)

            return response
        } catch (error) {
            commit('CLEAR_AUTH')
            throw error
        }
    },

    async getUserInfo({ commit, state }) {
        if (!state.token) {
            return Promise.reject(new Error('Token不存在'))
        }

        try {
            const response = await api.auth.getUserInfo()
            const userInfo = response.data.user

            commit('SET_USER_INFO', userInfo)
            return userInfo
        } catch (error) {
            commit('CLEAR_AUTH')
            throw error
        }
    },

    async logout({ commit }) {
        try {
            await api.auth.logout()
        } catch (error) {
            console.error('登出请求失败:', error)
        } finally {
            commit('CLEAR_AUTH')
        }
    }
}

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}