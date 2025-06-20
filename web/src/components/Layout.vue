<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px">
        <div class="sidebar">
          <div class="logo">
            <h2>会议室系统</h2>
          </div>

          <el-menu
              :default-active="$route.path"
              router
              background-color="#001529"
              text-color="#fff"
              active-text-color="#1890ff"
          >
            <el-menu-item index="/dashboard">
              <i class="el-icon-s-home"></i>
              <span>首页</span>
            </el-menu-item>

            <el-menu-item index="/booking">
              <i class="el-icon-calendar"></i>
              <span>预订会议室</span>
            </el-menu-item>

            <el-menu-item index="/my-bookings">
              <i class="el-icon-tickets"></i>
              <span>我的预订</span>
            </el-menu-item>

            <el-menu-item index="/checkin">
              <i class="el-icon-location-information"></i>
              <span>签到管理</span>
            </el-menu-item>

            <el-submenu index="/admin" v-if="isAdmin">
              <template slot="title">
                <i class="el-icon-setting"></i>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/rooms">会议室管理</el-menu-item>
              <el-menu-item index="/statistics">统计报表</el-menu-item>
            </el-submenu>
          </el-menu>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title">
                {{ $route.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" icon="el-icon-user-solid"></el-avatar>
                <span class="username">{{ userName }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters('auth', ['userInfo', 'isAdmin', 'userName'])
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.handleLogout()
      }
    },

    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await this.$store.dispatch('auth/logout')
        this.$message.success('退出成功')
        this.$router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('退出失败:', error)
        }
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  height: 100vh;
  background-color: #001529;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #002140;
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  color: #303133;
  font-weight: 500;
}

.main-content {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>