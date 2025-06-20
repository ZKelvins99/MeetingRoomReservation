import Cookies from 'js-cookie'

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
            // 模拟登录API调用
            const mockResponse = {
                data: {
                    token: 'mock-jwt-token-' + Date.now(),
                    user: {
                        id: loginForm.username === 'admin' ? 1 : 2,
                        username: loginForm.username,
                        realName: loginForm.username === 'admin' ? '系统管理员' : '普通用户',
                        role: loginForm.username === 'admin' ? 'ADMIN' : 'USER',
                        email: loginForm.username + '@company.com'
                    }
                }
            }

            const { token, user } = mockResponse.data

            commit('SET_TOKEN', token)
            commit('SET_USER_INFO', user)

            return mockResponse
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
            // 模拟获取用户信息
            const userInfo = state.userInfo
            if (userInfo) {
                commit('SET_USER_INFO', userInfo)
                return userInfo
            }
            throw new Error('用户信息不存在')
        } catch (error) {
            commit('CLEAR_AUTH')
            throw error
        }
    },

    async logout({ commit }) {
        try {
            // 模拟登出API调用
            console.log('用户登出')
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