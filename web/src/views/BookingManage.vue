<template>
  <div class="booking-manage">
    <div class="page-header">
      <h2>会议室预订</h2>
      <el-button type="primary" @click="showBookingDialog = true">
        <i class="el-icon-plus"></i> 新建预订
      </el-button>
    </div>

    <!-- 预订表单对话框 -->
    <el-dialog
        title="预订会议室"
        :visible.sync="showBookingDialog"
        width="600px"
        @closed="resetBookingForm"
    >
      <el-form
          :model="bookingForm"
          :rules="bookingRules"
          ref="bookingForm"
          label-width="100px"
      >
        <el-form-item label="会议室" prop="roomId">
          <el-select
              v-model="bookingForm.roomId"
              placeholder="请选择会议室"
              style="width: 100%"
          >
            <el-option
                v-for="room in rooms"
                :key="room.id"
                :label="room.roomName"
                :value="room.id"
            >
              <span>{{ room.roomName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                容量: {{ room.capacity }}人
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="会议主题" prop="meetingTitle">
          <el-input
              v-model="bookingForm.meetingTitle"
              placeholder="请输入会议主题"
          ></el-input>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                  v-model="bookingForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                  v-model="bookingForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="参会人数" prop="attendeeCount">
          <el-input-number
              v-model="bookingForm.attendeeCount"
              :min="1"
              :max="50"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>

        <el-form-item label="会议描述">
          <el-input
              v-model="bookingForm.meetingDesc"
              type="textarea"
              rows="3"
              placeholder="请输入会议描述（可选）"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="showBookingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBooking" :loading="bookingLoading">
          确认预订
        </el-button>
      </div>
    </el-dialog>

    <!-- 会议室列表 -->
    <div class="room-grid">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="room-card"
      >
        <div class="room-header">
          <h3>{{ room.roomName }}</h3>
          <el-tag :type="room.status === 1 ? 'success' : 'danger'">
            {{ room.status === 1 ? '可用' : '维护中' }}
          </el-tag>
        </div>

        <div class="room-info">
          <p><i class="el-icon-location"></i> {{ room.roomLocation }}</p>
          <p><i class="el-icon-user"></i> 容量: {{ room.capacity }}人</p>
        </div>

        <div class="room-actions">
          <el-button
              type="primary"
              size="small"
              :disabled="room.status !== 1"
              @click="quickBook(room)"
          >
            快速预订
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BookingManage',
  data() {
    return {
      showBookingDialog: false,
      bookingLoading: false,
      rooms: [
        { id: 1, roomName: '大会议室A', roomLocation: '1楼东侧', capacity: 20, status: 1 },
        { id: 2, roomName: '小会议室B', roomLocation: '2楼西侧', capacity: 8, status: 1 },
        { id: 3, roomName: '培训室C', roomLocation: '3楼北侧', capacity: 30, status: 1 },
        { id: 4, roomName: '讨论室D', roomLocation: '2楼东侧', capacity: 6, status: 1 }
      ],
      bookingForm: {
        roomId: '',
        meetingTitle: '',
        meetingDesc: '',
        startTime: '',
        endTime: '',
        attendeeCount: 1
      },
      bookingRules: {
        roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
        meetingTitle: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        attendeeCount: [{ required: true, message: '请输入参会人数', trigger: 'blur' }]
      }
    }
  },
  methods: {
    async handleBooking() {
      this.$refs.bookingForm.validate(async (valid) => {
        if (!valid) return

        if (this.bookingForm.endTime <= this.bookingForm.startTime) {
          this.$message.error('结束时间必须晚于开始时间')
          return
        }

        this.bookingLoading = true
        try {
          await this.$api.bookings.book(this.bookingForm)
          this.$message.success('预订成功')
          this.showBookingDialog = false
          this.resetBookingForm()
        } catch (error) {
          console.error('预订失败:', error)
          this.$message.error('预订失败，请稍后重试')
        } finally {
          this.bookingLoading = false
        }
      })
    },

    resetBookingForm() {
      this.$refs.bookingForm?.resetFields()
      this.bookingForm = {
        roomId: '',
        meetingTitle: '',
        meetingDesc: '',
        startTime: '',
        endTime: '',
        attendeeCount: 1
      }
    },

    quickBook(room) {
      this.bookingForm.roomId = room.id
      this.showBookingDialog = true
    }
  }
}
</script>

<style scoped>
.booking-manage {
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

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.room-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.room-info {
  margin-bottom: 20px;
}

.room-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.room-info i {
  margin-right: 8px;
  color: #909399;
}

.room-actions {
  display: flex;
  gap: 10px;
}
</style>