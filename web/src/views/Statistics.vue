<template>
  <div class="statistics">
    <div class="page-header">
      <h2>统计报表</h2>
      <el-button
          type="primary"
          icon="el-icon-refresh"
          @click="loadAllData"
          :loading="loading"
      >
        刷新数据
      </el-button>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-cards" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="overview-card">
            <div class="card-icon primary">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="card-content">
              <h3>{{ roomStats.totalRooms }}</h3>
              <p>会议室总数</p>
              <div class="sub-info">
                <span class="available">可用: {{ roomStats.availableRooms }}</span>
                <span class="maintenance">维护: {{ roomStats.maintenanceRooms }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-card">
            <div class="card-icon success">
              <i class="el-icon-calendar"></i>
            </div>
            <div class="card-content">
              <h3>{{ bookingStats.totalBookings }}</h3>
              <p>我的预订总数</p>
              <div class="sub-info">
                <span class="active">已预订: {{ bookingStats.bookedCount }}</span>
                <span class="cancelled">已取消: {{ bookingStats.cancelledCount }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-card">
            <div class="card-icon warning">
              <i class="el-icon-time"></i>
            </div>
            <div class="card-content">
              <h3>{{ bookingStats.todayBookings }}</h3>
              <p>今日预订</p>
              <div class="sub-info">
                <span class="today">{{ formatDate(new Date()) }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 会议室统计表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="data-table-card">
          <div slot="header">
            <h3>会议室状态统计</h3>
          </div>
          <el-table :data="roomList" v-loading="roomLoading" max-height="400">
            <el-table-column label="会议室名称" prop="roomName" min-width="120"></el-table-column>
            <el-table-column label="位置" prop="roomLocation" min-width="120"></el-table-column>
            <el-table-column label="容量" prop="capacity" align="center" width="80">
              <template slot-scope="scope">
                {{ scope.row.capacity }}人
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" align="center" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '可用' : '维护中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="data-table-card">
          <div slot="header">
            <h3>我的预订统计</h3>
          </div>
          <el-table :data="recentBookings" v-loading="bookingLoading" max-height="400">
            <el-table-column label="会议主题" prop="meetingTitle" min-width="140"></el-table-column>
            <el-table-column label="会议室" prop="roomName" min-width="100"></el-table-column>
            <el-table-column label="日期" prop="startTime" width="100">
              <template slot-scope="scope">
                {{ formatDate(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="bookingStatus" align="center" width="100">
              <template slot-scope="scope">
                <el-tag
                    :type="getBookingStatusType(scope.row.bookingStatus)"
                    size="small"
                >
                  {{ getBookingStatusText(scope.row.bookingStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 时间统计 -->
    <el-card class="time-stats-card">
      <div slot="header">
        <h3>时间统计</h3>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="time-stat-item">
            <div class="stat-label">本周预订</div>
            <div class="stat-value">{{ timeStats.weekBookings }}</div>
            <div class="stat-desc">{{ getWeekRange() }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="time-stat-item">
            <div class="stat-label">本月预订</div>
            <div class="stat-value">{{ timeStats.monthBookings }}</div>
            <div class="stat-desc">{{ getMonthRange() }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="time-stat-item">
            <div class="stat-label">最近更新</div>
            <div class="stat-value">{{ formatDateTime(lastUpdateTime) }}</div>
            <div class="stat-desc">数据更新时间</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Statistics',
  data() {
    return {
      loading: false,
      roomLoading: false,
      bookingLoading: false,
      lastUpdateTime: new Date(),

      // 会议室统计
      roomStats: {
        totalRooms: 0,
        availableRooms: 0,
        maintenanceRooms: 0
      },

      // 预订统计
      bookingStats: {
        totalBookings: 0,
        bookedCount: 0,
        cancelledCount: 0,
        todayBookings: 0
      },

      // 时间统计
      timeStats: {
        weekBookings: 0,
        monthBookings: 0
      },

      // 数据列表
      roomList: [],
      recentBookings: []
    }
  },

  mounted() {
    this.loadAllData()
  },

  methods: {
    // 加载所有数据
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadRoomStats(),
          this.loadBookingStats()
        ])
        this.lastUpdateTime = new Date()
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.$message.error('加载统计数据失败')
      } finally {
        this.loading = false
      }
    },

    // 加载会议室统计
    async loadRoomStats() {
      this.roomLoading = true
      try {
        const response = await this.$api.rooms.getAll()
        this.roomList = response.data || []

        // 计算会议室统计
        this.roomStats.totalRooms = this.roomList.length
        this.roomStats.availableRooms = this.roomList.filter(room => room.status === 1).length
        this.roomStats.maintenanceRooms = this.roomList.filter(room => room.status === 0).length

        console.log('会议室统计:', this.roomStats)
      } catch (error) {
        console.error('获取会议室统计失败:', error)
        this.$message.error('获取会议室统计失败')
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
        this.recentBookings = bookings.slice(0, 10) // 只显示最近10条

        // 计算预订统计
        this.bookingStats.totalBookings = bookings.length
        this.bookingStats.bookedCount = bookings.filter(b => b.bookingStatus === 'BOOKED').length
        this.bookingStats.cancelledCount = bookings.filter(b => b.bookingStatus === 'CANCELLED').length

        // 计算今日预订
        const today = new Date()
        const todayStr = this.formatDate(today)
        this.bookingStats.todayBookings = bookings.filter(booking => {
          const bookingDate = this.formatDate(new Date(booking.startTime))
          return bookingDate === todayStr
        }).length

        // 计算时间统计
        this.calculateTimeStats(bookings)

        console.log('预订统计:', this.bookingStats)
      } catch (error) {
        console.error('获取预订统计失败:', error)
        this.$message.error('获取预订统计失败')
      } finally {
        this.bookingLoading = false
      }
    },

    // 计算时间统计
    calculateTimeStats(bookings) {
      const now = new Date()

      // 本周统计
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay()) // 本周开始
      weekStart.setHours(0, 0, 0, 0)

      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6) // 本周结束
      weekEnd.setHours(23, 59, 59, 999)

      this.timeStats.weekBookings = bookings.filter(booking => {
        const bookingTime = new Date(booking.createTime)
        return bookingTime >= weekStart && bookingTime <= weekEnd
      }).length

      // 本月统计
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)

      this.timeStats.monthBookings = bookings.filter(booking => {
        const bookingTime = new Date(booking.createTime)
        return bookingTime >= monthStart && bookingTime <= monthEnd
      }).length
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

    // 获取本周时间范围
    getWeekRange() {
      const now = new Date()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())

      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)

      return `${this.formatDate(weekStart)} - ${this.formatDate(weekEnd)}`
    },

    // 获取本月时间范围
    getMonthRange() {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth() + 1

      return `${year}年${month}月`
    },

    // 格式化日期
    formatDate(dateTime) {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')

      return `${year}-${month}-${day}`
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')

      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.overview-cards {
  margin-bottom: 30px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.overview-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.card-icon {
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

.card-icon.primary { background: #409eff; }
.card-icon.success { background: #67c23a; }
.card-icon.warning { background: #e6a23c; }
.card-icon.danger { background: #f56c6c; }

.card-content {
  flex: 1;
}

.card-content h3 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.card-content p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.sub-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.sub-info span {
  padding: 2px 8px;
  border-radius: 4px;
  background: #f5f7fa;
  color: #606266;
}

.sub-info .available { background: #f0f9ff; color: #1890ff; }
.sub-info .maintenance { background: #fff2e8; color: #fa8c16; }
.sub-info .active { background: #f6ffed; color: #52c41a; }
.sub-info .cancelled { background: #fff1f0; color: #ff4d4f; }
.sub-info .today { background: #fff7e6; color: #fa8c16; }

.data-table-card {
  margin-bottom: 20px;
}

.data-table-card .el-card__header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.time-stats-card .el-card__header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.time-stat-item {
  text-align: center;
  padding: 20px;
  background: #fafbfc;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .overview-cards .el-col {
    margin-bottom: 20px;
  }

  .overview-card {
    flex-direction: column;
    text-align: center;
  }

  .card-content h3 {
    font-size: 24px;
  }
}
</style>