import { login as loginApi, getCurrentUser } from '@/api/modules/auth'

const user = JSON.parse(localStorage.getItem('user') || 'null')

const state = {
  token: localStorage.getItem('token') || '',
  user
}

const getters = {
  isLoggedIn: state => !!state.token,
  isAdmin: state => !!(state.user && state.user.role === 'ADMIN'),
  nickname: state => (state.user && state.user.nickname) || '',
  permissions: state => (state.user && state.user.permissions) || [],
  hasPermission: state => perm => (state.user && state.user.permissions || []).includes(perm)
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_USER(state, user) {
    state.user = user
    if (user) {
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      localStorage.removeItem('user')
    }
  }
}

const actions = {
  async login({ commit }, credentials) {
    const res = await loginApi(credentials)
    commit('SET_TOKEN', res.data.token)
    commit('SET_USER', res.data.user)
    return res
  },
  logout({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER', null)
  },
  async fetchCurrentUser({ commit }) {
    const res = await getCurrentUser()
    commit('SET_USER', res.data)
    return res
  }
}

export default {
  state,
  getters,
  mutations,
  actions
}
