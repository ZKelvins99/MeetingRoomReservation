<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来，{{ userInfo.realName || userInfo.username || 'ZKelvins99' }}！</h1>
        <p>今天是 {{ currentDate }}，{{ currentTime }}</p>
        <div class="weather-info">
          <span>祝您工作愉快 🌟</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon primary">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <h3>{{ roomStats.totalRooms }}</h3>
              <p>会议室总数</p>
              <div class="sub-text">{{ roomStats.availableRooms }} 个可用</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon success">
              <i class="el-icon-calendar"></i>
            </div>
            <div class="stat-info">
              <h3>{{ bookingStats.todayBookings }}</h3>
              <p>今日我的预订</p>
              <div class="sub-text">{{ formatDate(new Date()) }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon warning">
              <i class="el-icon-tickets"></i>
            </div>
            <div class="stat-info">
              <h3>{{ bookingStats.totalBookings }}</h3>
              <p>我的总预订</p>
              <div class="sub-text">{{ bookingStats.activeBookings }} 个进行中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon info">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <h3>{{ bookingStats.thisWeekBookings }}</h3>
              <p>本周预订</p>
              <div class="sub-text">{{ getWeekRange() }}</div>
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
                @click="$router.push('/bookings')"
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

            <el-button
                v-if="isAdmin"
                type="danger"
                size="large"
                icon="el-icon-setting"
                @click="$router.push('/rooms')"
            >
              会议室管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日预订和最近活动 -->
    <el-row :gutter="20" class="dashboard-content">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <h3>今日我的预订</h3>
            <el-button
                type="text"
                size="small"
                @click="$router.push('/my-bookings')"
            >
              查看全部
            </el-button>
          </div>

          <div class="today-bookings" v-loading="bookingLoading">
            <div
                v-if="todayBookingList.length > 0"
                v-for="booking in todayBookingList"
                :key="booking.id"
                class="booking-item"
            >
              <div class="booking-info">
                <h4>{{ booking.meetingTitle }}</h4>
                <p>
                  <i class="el-icon-office-building"></i>
                  {{ booking.roomName }}
                </p>
                <p>
                  <i class="el-icon-time"></i>
                  {{ formatTime(booking.startTime) }} - {{ formatTime(booking.endTime) }}
                </p>
              </div>
              <div class="booking-status">
                <el-tag
                    :type="getBookingStatusType(booking.bookingStatus)"
                    size="small"
                >
                  {{ getBookingStatusText(booking.bookingStatus) }}
                </el-tag>
              </div>
            </div>

            <el-empty
                v-else
                description="今日暂无预订"
                :image-size="80"
            ></el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">
            <h3>会议室状态</h3>
            <el-button
                type="text"
                size="small"
                @click="$router.push('/rooms')"
            >
              管理会议室
            </el-button>
          </div>

          <div class="room-status" v-loading="roomLoading">
            <div
                v-for="room in recentRoomList"
                :key="room.id"
                class="room-item"
            >
              <div class="room-info">
                <h4>{{ room.roomName }}</h4>
                <p>
                  <i class="el-icon-location"></i>
                  {{ room.roomLocation }}
                </p>
                <p>
                  <i class="el-icon-user"></i>
                  容量：{{ room.capacity }}人
                </p>
              </div>
              <div class="room-status-tag">
                <el-tag
                    :type="room.status === 1 ? 'success' : 'danger'"
                    size="small"
                >
                  {{ room.status === 1 ? '可用' : '维护中' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      currentTime: '',
      timeInterval: null,
      loading: false,
      bookingLoading: false,
      roomLoading: false,

      // 用户信息
      userInfo: {},

      // 统计数据
      roomStats: {
        totalRooms: 0,
        availableRooms: 0
      },
      bookingStats: {
        totalBookings: 0,
        todayBookings: 0,
        activeBookings: 0,
        thisWeekBookings: 0
      },

      // 列表数据
      todayBookingList: [],
      recentRoomList: []
    }
  },

  computed: {
    ...mapGetters('auth', ['isAdmin']),

    currentDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekday = weekdays[now.getDay()]

      return `${year}年${month}月${day}日 ${weekday}`
    }
  },

  created() {
    this.updateTime()
    this.timeInterval = setInterval(this.updateTime, 1000)
  },

  mounted() {
    this.loadDashboardData()
  },

  beforeDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval)
    }
  },

  methods: {
    // 更新时间
    updateTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${hours}:${minutes}:${seconds}`
    },

    // 加载仪表板数据
    async loadDashboardData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadUserInfo(),
          this.loadRoomStats(),
          this.loadBookingStats()
        ])
      } catch (error) {
        console.error('加载仪表板数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },

    // 加载用户信息
    async loadUserInfo() {
      try {
        const response = await this.$api.auth.getUserInfo()
        this.userInfo = response.data || {}
      } catch (error) {
        console.warn('获取用户信息失败:', error)
        // 使用默认信息
        this.userInfo = { username: 'ZKelvins99', realName: 'ZKelvins99' }
      }
    },

    // 加载会议室统计
    async loadRoomStats() {
      this.roomLoading = true
      try {
        const response = await this.$api.rooms.getAll()
        const rooms = response.data || []

        this.roomStats.totalRooms = rooms.length
        this.roomStats.availableRooms = rooms.filter(room => room.status === 1).length

        // 显示前5个会议室
        this.recentRoomList = rooms.slice(0, 5)
      } catch (error) {
        console.error('获取会议室统计失败:', error)
      } finally {
        this.roomLoading = false
      }
    },

    // 加载预订统计
    async loadBookingStats() {
      this.bookingLoading = true
      try {
        const response = await this.$api.bookings.getMy()
        const bookings = response.data.records || []

        this.bookingStats.totalBookings = bookings.length

        // 计算今日预订
        const today = new Date()
        const todayStr = this.formatDate(today)

        const todayBookings = bookings.filter(booking => {
          const bookingDate = this.formatDate(new Date(booking.startTime))
          return bookingDate === todayStr
        })

        this.bookingStats.todayBookings = todayBookings.length
        this.todayBookingList = todayBookings

        // 计算进行中的预订
        const now = new Date()
        this.bookingStats.activeBookings = bookings.filter(booking => {
          const startTime = new Date(booking.startTime)
          const endTime = new Date(booking.endTime)
          return now >= startTime && now <= endTime && booking.bookingStatus === 'BOOKED'
        }).length

        // 计算本周预订
        this.bookingStats.thisWeekBookings = this.calculateWeekBookings(bookings)

      } catch (error) {
        console.error('获取预订统计失败:', error)
      } finally {
        this.bookingLoading = false
      }
    },

    // 计算本周预订
    calculateWeekBookings(bookings) {
      const now = new Date()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())
      weekStart.setHours(0, 0, 0, 0)

      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)
      weekEnd.setHours(23, 59, 59, 999)

      return bookings.filter(booking => {
        const bookingTime = new Date(booking.startTime)
        return bookingTime >= weekStart && bookingTime <= weekEnd
      }).length
    },

    // 获取本周时间范围
    getWeekRange() {
      const now = new Date()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())

      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)

      return `${this.formatDate(weekStart)} - ${this.formatDate(weekEnd)}`
    },

    // 获取预订状态类型
    getBookingStatusType(status) {
      switch (status) {
        case 'BOOKED':
          return 'success'
        case 'CANCELLED':
          return 'danger'
        case 'COMPLETED':
          return 'info'
        default:
          return 'info'
      }
    },

    // 获取预订状态文本
    getBookingStatusText(status) {
      switch (status) {
        case 'BOOKED':
          return '已预订'
        case 'CANCELLED':
          return '已取消'
        case 'COMPLETED':
          return '已完成'
        default:
          return '未知'
      }
    },

    // 格式化时间
    formatTime(dateTime) {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')

      return `${hours}:${minutes}`
    },

    // 格式化日期
    formatDate(dateTime) {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')

      return `${year}-${month}-${day}`
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
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-content h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-content p {
  margin: 0 0 10px 0;
  opacity: 0.9;
  font-size: 16px;
}

.weather-info {
  opacity: 0.8;
  font-size: 14px;
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
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.stat-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
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
  flex-shrink: 0;
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

.stat-icon.info {
  background: #909399;
}

.stat-info {
  flex: 1;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-info p {
  margin: 0 0 5px 0;
  color: #606266;
  font-size: 14px;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.quick-actions {
  margin-bottom: 20px;
}

.quick-actions .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions .el-card__header h3 {
  margin: 0;
  color: #303133;
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
  border-radius: 8px;
}

.dashboard-content {
  margin-top: 20px;
}

.dashboard-content .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-content .el-card__header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.today-bookings,
.room-status {
  max-height: 300px;
  overflow-y: auto;
}

.booking-item,
.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.booking-item:last-child,
.room-item:last-child {
  border-bottom: none;
}

.booking-info h4,
.room-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.booking-info p,
.room-info p {
  margin: 4px 0;
  color: #606266;
  font-size: 13px;
  display: flex;
  align-items: center;
}

.booking-info i,
.room-info i {
  margin-right: 5px;
  color: #909399;
  width: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-cards .el-col {
    margin-bottom: 15px;
  }

  .action-buttons {
    justify-content: center;
  }

  .action-buttons .el-button {
    flex: 1;
    min-width: 120px;
  }

  .booking-item,
  .room-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>