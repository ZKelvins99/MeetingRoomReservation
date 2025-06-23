<template>
  <div class="my-bookings">
    <div class="page-header">
      <h2>我的预订</h2>
      <el-button
          type="primary"
          icon="el-icon-refresh"
          @click="refreshBookings"
          :loading="loading"
      >
        刷新
      </el-button>
    </div>

    <!-- 预订列表 -->
    <div class="booking-list" v-if="bookings.length > 0">
      <div
          v-for="booking in bookings"
          :key="booking.id"
          class="booking-card"
          :class="{ 'cancelled': booking.bookingStatus === 'CANCELLED' }"
      >
        <div class="booking-header">
          <div class="booking-title">
            <h3>{{ booking.meetingTitle }}</h3>
            <el-tag
                :type="getStatusTagType(booking.bookingStatus)"
            >
              {{ getStatusText(booking.bookingStatus) }}
            </el-tag>
          </div>
          <div class="booking-time">
            {{ formatDateTime(booking.startTime, 'MM-DD HH:mm') }} -
            {{ formatDateTime(booking.endTime, 'HH:mm') }}
          </div>
        </div>

        <div class="booking-content">
          <div class="booking-info">
            <div class="info-item">
              <i class="el-icon-office-building"></i>
              <span>{{ booking.roomName }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span>{{ booking.attendeeCount }} 人参会</span>
            </div>
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span>创建时间：{{ formatDateTime(booking.createTime, 'YYYY-MM-DD HH:mm') }}</span>
            </div>
          </div>

          <!-- 会议描述 -->
          <div class="meeting-desc" v-if="booking.meetingDesc">
            <p><strong>会议描述：</strong>{{ booking.meetingDesc }}</p>
          </div>
        </div>

        <div class="booking-actions">
          <el-button type="info" size="small" @click="viewDetails(booking)">
            查看详情
          </el-button>
          <el-button
              v-if="booking.bookingStatus === 'BOOKED'"
              type="warning"
              size="small"
              @click="cancelBooking(booking)"
              :loading="cancelLoading === booking.id"
          >
            取消预订
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无预订记录" v-loading="loading"></el-empty>

    <!-- 详情对话框 -->
    <el-dialog
        title="预订详情"
        :visible.sync="detailDialogVisible"
        width="50%"
        @close="closeDetailDialog"
    >
      <div class="booking-detail" v-if="selectedBooking">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>会议标题：</label>
              <span>{{ selectedBooking.meetingTitle }}</span>
            </div>
            <div class="detail-item">
              <label>会议室：</label>
              <span>{{ selectedBooking.roomName }}</span>
            </div>
            <div class="detail-item">
              <label>预订状态：</label>
              <el-tag :type="getStatusTagType(selectedBooking.bookingStatus)">
                {{ getStatusText(selectedBooking.bookingStatus) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>参会人数：</label>
              <span>{{ selectedBooking.attendeeCount }} 人</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>时间信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>开始时间：</label>
              <span>{{ formatDateTime(selectedBooking.startTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
            <div class="detail-item">
              <label>结束时间：</label>
              <span>{{ formatDateTime(selectedBooking.endTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
            <div class="detail-item">
              <label>提醒时间：</label>
              <span>{{ selectedBooking.remindTime }} 分钟前</span>
            </div>
            <div class="detail-item">
              <label>是否已提醒：</label>
              <el-tag :type="selectedBooking.isReminded ? 'success' : 'info'">
                {{ selectedBooking.isReminded ? '已提醒' : '未提醒' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>其他信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ formatDateTime(selectedBooking.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间：</label>
              <span>{{ formatDateTime(selectedBooking.updateTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
            <div class="detail-item" v-if="selectedBooking.meetingDesc">
              <label>会议描述：</label>
              <span>{{ selectedBooking.meetingDesc }}</span>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
            v-if="selectedBooking && selectedBooking.bookingStatus === 'BOOKED'"
            type="warning"
            @click="cancelBookingFromDetail"
            :loading="cancelLoading === selectedBooking.id"
        >
          取消预订
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MyBookings',
  data() {
    return {
      bookings: [],
      loading: false,
      cancelLoading: null,
      detailDialogVisible: false,
      selectedBooking: null
    }
  },
  mounted() {
    this.loadBookings()
  },
  methods: {
    // 加载预订列表
    async loadBookings() {
      this.loading = true
      try {
        const response = await this.$api.bookings.getMy()
        this.bookings = response.data.records || []
        console.log('获取预订列表成功:', this.bookings)
      } catch (error) {
        console.error('获取预订列表失败:', error)
        this.$message.error('获取预订列表失败')
      } finally {
        this.loading = false
      }
    },

    // 刷新预订列表
    refreshBookings() {
      this.loadBookings()
      this.$message.success('刷新成功')
    },

    // 查看详情
    viewDetails(booking) {
      this.selectedBooking = booking
      this.detailDialogVisible = true
    },

    // 关闭详情对话框
    closeDetailDialog() {
      this.selectedBooking = null
      this.detailDialogVisible = false
    },

    // 取消预订
    async cancelBooking(booking) {
      try {
        await this.$confirm('确定要取消这个预订吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.cancelLoading = booking.id
        await this.$api.bookings.cancel(booking.id)
        this.$message.success('取消预订成功')

        // 重新加载列表
        await this.loadBookings()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消预订失败:', error)
          this.$message.error('取消预订失败')
        }
      } finally {
        this.cancelLoading = null
      }
    },

    // 从详情页取消预订
    async cancelBookingFromDetail() {
      await this.cancelBooking(this.selectedBooking)
      this.detailDialogVisible = false
    },

    // 格式化日期时间
    formatDateTime(dateTime, format = 'YYYY-MM-DD HH:mm:ss') {
      if (!dateTime) return ''

      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')

      return format
          .replace('YYYY', year)
          .replace('MM', month)
          .replace('DD', day)
          .replace('HH', hours)
          .replace('mm', minutes)
          .replace('ss', seconds)
    },

    // 获取状态标签类型
    getStatusTagType(status) {
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

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'BOOKED':
          return '已预订'
        case 'CANCELLED':
          return '已取消'
        case 'COMPLETED':
          return '已完成'
        default:
          return '未知状态'
      }
    }
  }
}
</script>

<style scoped>
.my-bookings {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.booking-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  border-left: 4px solid #409eff;
  transition: all 0.3s ease;
}

.booking-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.booking-card.cancelled {
  border-left-color: #f56c6c;
  opacity: 0.8;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.booking-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.booking-title h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.booking-time {
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
}

.booking-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.info-item i {
  color: #909399;
}

.meeting-desc {
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
}

.booking-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 详情对话框样式 */
.booking-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 5px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  margin-right: 10px;
}

.detail-item span {
  color: #303133;
  word-break: break-all;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .booking-header {
    flex-direction: column;
    gap: 10px;
  }

  .booking-info {
    flex-direction: column;
    gap: 10px;
  }

  .booking-actions {
    justify-content: center;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>