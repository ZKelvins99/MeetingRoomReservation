const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          // 如果后端接口有 /api 前缀，保持原样
          // 如果没有，可以去掉前缀：'^/api': ''
        },
        onProxyReq: function(proxyReq, req, res) {
          console.log('代理请求:', req.method, req.url)
        },
        onError: function(err, req, res) {
          console.log('代理错误:', err)
        }
      }
    }
  },
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',
  productionSourceMap: false
})