<template>
  <div class="checkin-manage">
    <div class="page-header">
      <h2>签到管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-qrcode">
          扫码签到
        </el-button>
        <el-button type="success" icon="el-icon-edit">
          手动签到
        </el-button>
      </div>
    </div>

    <!-- 当前可签到的会议 -->
    <el-card class="current-meetings">
      <div slot="header">
        <h3>当前可签到的会议</h3>
      </div>

      <div class="meeting-grid">
        <div
            v-for="meeting in currentMeetings"
            :key="meeting.id"
            class="meeting-card"
        >
          <div class="meeting-header">
            <h4>{{ meeting.meetingTitle }}</h4>
            <el-tag type="success" size="small">进行中</el-tag>
          </div>
          <div class="meeting-info">
            <p><i class="el-icon-office-building"></i> {{ meeting.roomName }}</p>
            <p><i class="el-icon-time"></i> {{ meeting.startTime | formatTime }} - {{ meeting.endTime | formatTime }}</p>
            <p><i class="el-icon-user"></i> 已签到 {{ meeting.checkedInCount || 0 }}/{{ meeting.attendeeCount }} 人</p>
          </div>
          <div class="meeting-actions">
            <el-button type="primary" size="small">立即签到</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 签到历史 -->
    <el-card class="checkin-history">
      <div slot="header">
        <h3>签到历史</h3>
      </div>

      <el-table :data="checkinHistory">
        <el-table-column label="会议主题" prop="meetingTitle"></el-table-column>
        <el-table-column label="签到时间" prop="checkinTime" width="180">
          <template slot-scope="scope">
            {{ scope.row.checkinTime | formatDateTime }}
          </template>
        </el-table-column>
        <el-table-column label="签到方式" prop="checkinType" width="120">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.checkinType === 'QR_CODE' ? '扫码' : '手动' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'CheckinManage',
  data() {
    return {
      currentMeetings: [
        {
          id: 1,
          meetingTitle: '产品评审会议',
          roomName: '大会议室A',
          startTime: new Date(Date.now() - 30 * 60000),
          endTime: new Date(Date.now() + 90 * 60000),
          attendeeCount: 8,
          checkedInCount: 5
        }
      ],
      checkinHistory: [
        {
          id: 1,
          meetingTitle: '周例会',
          checkinTime: new Date(Date.now() - 2 * 24 * 60 * 60000),
          checkinType: 'QR_CODE'
        }
      ]
    }
  }
}
</script>

<style scoped>
.checkin-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.current-meetings {
  margin-bottom: 20px;
}

.meeting-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.meeting-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.meeting-header h4 {
  margin: 0;
  color: #303133;
}

.meeting-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.meeting-info i {
  margin-right: 8px;
  color: #909399;
}

.meeting-actions {
  text-align: right;
}
</style>