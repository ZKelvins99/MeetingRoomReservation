<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来，{{ userName }}！</h1>
        <p>今天是 {{ currentDate }}，{{ currentTime }}</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon primary">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <h3>8</h3>
              <p>会议室总数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon success">
              <i class="el-icon-calendar"></i>
            </div>
            <div class="stat-info">
              <h3>12</h3>
              <p>今日预订</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon warning">
              <i class="el-icon-tickets"></i>
            </div>
            <div class="stat-info">
              <h3>3</h3>
              <p>我的预订</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon danger">
              <i class="el-icon-location-information"></i>
            </div>
            <div class="stat-info">
              <h3>28</h3>
              <p>今日签到</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <h3>快速操作</h3>
          </div>

          <div class="action-buttons">
            <el-button
                type="primary"
                size="large"
                icon="el-icon-plus"
                @click="$router.push('/booking')"
            >
              预订会议室
            </el-button>

            <el-button
                type="success"
                size="large"
                icon="el-icon-location-information"
                @click="$router.push('/checkin')"
            >
              会议签到
            </el-button>

            <el-button
                type="info"
                size="large"
                icon="el-icon-view"
                @click="$router.push('/my-bookings')"
            >
              我的预订
            </el-button>

            <el-button
                v-if="isAdmin"
                type="warning"
                size="large"
                icon="el-icon-data-analysis"
                @click="$router.push('/statistics')"
            >
              统计报表
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      currentTime: '',
      timeInterval: null
    }
  },
  computed: {
    ...mapGetters('auth', ['userName', 'isAdmin']),

    currentDate() {
      return this.$moment().format('YYYY年MM月DD日 dddd')
    }
  },
  created() {
    this.updateTime()
    this.timeInterval = setInterval(this.updateTime, 1000)
  },
  beforeDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval)
    }
  },
  methods: {
    updateTime() {
      this.currentTime = this.$moment().format('HH:mm:ss')
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.welcome-content h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.primary {
  background: #409eff;
}

.stat-icon.success {
  background: #67c23a;
}

.stat-icon.warning {
  background: #e6a23c;
}

.stat-icon.danger {
  background: #f56c6c;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  height: 50px;
  padding: 0 30px;
  font-size: 16px;
}
</style>