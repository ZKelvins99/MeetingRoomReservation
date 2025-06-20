<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    // 检查用户登录状态
    if (this.$store.getters['auth/token'] && this.$route.path !== '/login') {
      this.$store.dispatch('auth/getUserInfo').catch(() => {
        this.$store.dispatch('auth/logout')
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style>
#app {
  height: 100vh;
  overflow: hidden;
}
</style>