<template>
  <div class="meeting-rooms">
    <div class="page-header">
      <h2>会议室管理</h2>
      <el-button type="primary" icon="el-icon-plus">
        添加会议室
      </el-button>
    </div>

    <div class="rooms-grid">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="room-card"
      >
        <div class="room-header">
          <div class="room-title">
            <h3>{{ room.roomName }}</h3>
            <el-tag :type="room.status === 1 ? 'success' : 'danger'" size="small">
              {{ room.status === 1 ? '可用' : '维护中' }}
            </el-tag>
          </div>
        </div>

        <div class="room-info">
          <div class="info-item">
            <i class="el-icon-location"></i>
            <span>{{ room.roomLocation }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-user"></i>
            <span>容量：{{ room.capacity }}人</span>
          </div>
        </div>

        <div class="room-footer">
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MeetingRooms',
  data() {
    return {
      rooms: [
        { id: 1, roomName: '大会议室A', roomLocation: '1楼东侧', capacity: 20, status: 1 },
        { id: 2, roomName: '小会议室B', roomLocation: '2楼西侧', capacity: 8, status: 1 },
        { id: 3, roomName: '培训室C', roomLocation: '3楼北侧', capacity: 30, status: 0 },
        { id: 4, roomName: '讨论室D', roomLocation: '2楼东侧', capacity: 6, status: 1 }
      ]
    }
  }
}
</script>

<style scoped>
.meeting-rooms {
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

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.room-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
  background: white;
}

.room-header {
  margin-bottom: 15px;
}

.room-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-title h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.room-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.info-item i {
  margin-right: 8px;
  color: #909399;
}

.room-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>