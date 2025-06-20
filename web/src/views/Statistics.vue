<template>
  <div class="statistics">
    <div class="page-header">
      <h2>统计报表</h2>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon primary">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="card-content">
              <h3>8</h3>
              <p>会议室总数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon success">
              <i class="el-icon-calendar"></i>
            </div>
            <div class="card-content">
              <h3>245</h3>
              <p>总预订数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon warning">
              <i class="el-icon-user"></i>
            </div>
            <div class="card-content">
              <h3>45</h3>
              <p>活跃用户</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon danger">
              <i class="el-icon-location-information"></i>
            </div>
            <div class="card-content">
              <h3>218</h3>
              <p>总签到数</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表 -->
    <el-card class="data-table-card">
      <div slot="header">
        <h3>会议室使用统计</h3>
      </div>

      <el-table :data="tableData">
        <el-table-column label="会议室名称" prop="roomName"></el-table-column>
        <el-table-column label="总预订数" prop="totalBookings" align="center"></el-table-column>
        <el-table-column label="使用时长" prop="totalHours" align="center"></el-table-column>
        <el-table-column label="使用率" prop="usageRate" align="center">
          <template slot-scope="scope">
            {{ scope.row.usageRate }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Statistics',
  data() {
    return {
      tableData: [
        { roomName: '大会议室A', totalBookings: 45, totalHours: 180, usageRate: 75 },
        { roomName: '小会议室B', totalBookings: 32, totalHours: 96, usageRate: 60 },
        { roomName: '培训室C', totalBookings: 28, totalHours: 140, usageRate: 45 },
        { roomName: '讨论室D', totalBookings: 56, totalHours: 168, usageRate: 85 }
      ]
    }
  }
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
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
}

.card-icon.primary { background: #409eff; }
.card-icon.success { background: #67c23a; }
.card-icon.warning { background: #e6a23c; }
.card-icon.danger { background: #f56c6c; }

.card-content h3 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.card-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>