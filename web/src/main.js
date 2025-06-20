import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/css/global.css'
import api from './api'
import moment from 'moment'

Vue.config.productionTip = false

// 设置中文
import locale from 'element-ui/lib/locale/lang/zh-CN'
Vue.use(ElementUI, { locale })

// 设置moment中文
moment.locale('zh-cn')

// 全局注册API
Vue.prototype.$api = api
Vue.prototype.$moment = moment

// 全局过滤器
Vue.filter('formatDate', function (value, format = 'YYYY-MM-DD HH:mm') {
  if (!value) return ''
  return moment(value).format(format)
})

Vue.filter('formatDateTime', function (value) {
  if (!value) return ''
  return moment(value).format('YYYY-MM-DD HH:mm:ss')
})

Vue.filter('formatTime', function (value) {
  if (!value) return ''
  return moment(value).format('HH:mm')
})

Vue.filter('fromNow', function (value) {
  if (!value) return ''
  return moment(value).fromNow()
})

// 全局混入
Vue.mixin({
  methods: {
    // 通用确认对话框
    $confirmDelete(message = '确定要删除吗？') {
      return this.$confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    },
    
    // 通用成功提示
    $successMsg(message) {
      this.$message({
        type: 'success',
        message: message,
        duration: 2000
      })
    },
    
    // 通用错误提示
    $errorMsg(message) {
      this.$message({
        type: 'error',
        message: message,
        duration: 3000
      })
    }
  }
})

new Vue({
  router,
  store,
  render: h => h(App),
  mounted() {
    // 隐藏加载动画
    const loading = document.querySelector('.loading-container')
    if (loading) {
      loading.style.display = 'none'
    }
  }
}).$mount('#app')