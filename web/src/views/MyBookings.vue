<template>
  <div class="my-bookings">
    <div class="page-header">
      <h2>我的预订</h2>
    </div>

    <!-- 预订列表 -->
    <div class="booking-list" v-if="bookings.length > 0">
      <div
          v-for="booking in bookings"
          :key="booking.id"
          class="booking-card"
      >
        <div class="booking-header">
          <div class="booking-title">
            <h3>{{ booking.meetingTitle }}</h3>
            <el-tag type="success">已预订</el-tag>
          </div>
          <div class="booking-time">
            {{ booking.startTime | formatDate('MM-DD HH:mm') }} -
            {{ booking.endTime | formatDate('HH:mm') }}
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
          </div>
        </div>

        <div class="booking-actions">
          <el-button type="info" size="small">查看详情</el-button>
          <el-button type="warning" size="small" @click="cancelBooking(booking)">取消预订</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无预订记录"></el-empty>
  </div>
</template>

<script>
export default {
  name: 'MyBookings',
  data() {
    return {
      bookings: [
        {
          id: 1,
          meetingTitle: '项目讨论会',
          roomName: '大会议室A',
          startTime: new Date(),
          endTime: new Date(Date.now() + 2 * 60 * 60 * 1000),
          attendeeCount: 5
        },
        {
          id: 2,
          meetingTitle: '周例会',
          roomName: '小会议室B',
          startTime: new Date(Date.now() + 24 * 60 * 60 * 1000),
          endTime: new Date(Date.now() + 24 * 60 * 60 * 1000 + 60 * 60 * 1000),
          attendeeCount: 3
        }
      ]
    }
  },
  methods: {
    async cancelBooking(booking) {
      try {
        await this.$confirm('确定要取消这个预订吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await this.$api.bookings.cancel(booking.id)
        this.$message.success('取消预订成功')
        // 重新加载列表
        this.loadBookings()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消预订失败:', error)
          this.$message.error('取消预订失败')
        }
      }
    },

    loadBookings() {
      // 重新加载预订列表的逻辑
    }
  }
}
</script>

<style scoped>
.my-bookings {
  padding: 20px;
}

.page-header {
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
}

.booking-info {
  display: flex;
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

.booking-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>