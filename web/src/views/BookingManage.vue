<template>
  <div class="booking-manage">
    <div class="page-header">
      <h2>会议室预订</h2>
      <div class="header-actions">
        <el-button
            type="default"
            icon="el-icon-refresh"
            @click="loadRooms"
            :loading="loading"
        >
          刷新
        </el-button>
        <el-button type="primary" @click="showBookingDialog = true">
          <i class="el-icon-plus"></i> 新建预订
        </el-button>
      </div>
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
              @change="onRoomChange"
          >
            <el-option
                v-for="room in availableRooms"
                :key="room.id"
                :label="room.roomName"
                :value="room.id"
            >
              <span>{{ room.roomName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                容量: {{ room.capacity }}人 | {{ room.roomLocation }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="会议主题" prop="meetingTitle">
          <el-input
              v-model="bookingForm.meetingTitle"
              placeholder="请输入会议主题"
              maxlength="100"
              show-word-limit
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
                  :picker-options="startTimeOptions"
                  format="yyyy-MM-dd HH:mm"
                  @change="onStartTimeChange"
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
                  :picker-options="endTimeOptions"
                  format="yyyy-MM-dd HH:mm"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="参会人数" prop="attendeeCount">
          <el-input-number
              v-model="bookingForm.attendeeCount"
              :min="1"
              :max="selectedRoomCapacity"
              style="width: 100%"
              placeholder="请输入参会人数"
          ></el-input-number>
          <div class="form-hint" v-if="selectedRoom">
            当前会议室最大容量：{{ selectedRoom.capacity }}人
          </div>
        </el-form-item>

        <el-form-item label="提醒时间">
          <el-select v-model="bookingForm.remindTime" style="width: 100%">
            <el-option label="不提醒" :value="0"></el-option>
            <el-option label="提前5分钟" :value="5"></el-option>
            <el-option label="提前15分钟" :value="15"></el-option>
            <el-option label="提前30分钟" :value="30"></el-option>
            <el-option label="提前60分钟" :value="60"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="会议描述">
          <el-input
              v-model="bookingForm.meetingDesc"
              type="textarea"
              rows="3"
              placeholder="请输入会议描述（可选）"
              maxlength="500"
              show-word-limit
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
    <div class="room-grid" v-loading="loading">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="room-card"
          :class="{ 'disabled': room.status !== 1 }"
      >
        <div class="room-header">
          <h3>{{ room.roomName }}</h3>
          <el-tag :type="room.status === 1 ? 'success' : 'danger'">
            {{ room.status === 1 ? '可用' : '维护中' }}
          </el-tag>
        </div>

        <div class="room-info">
          <div class="info-item">
            <i class="el-icon-location"></i>
            <span>{{ room.roomLocation }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-user"></i>
            <span>容量: {{ room.capacity }}人</span>
          </div>
          <div class="info-item" v-if="room.equipment">
            <i class="el-icon-monitor"></i>
            <span>设备: {{ getEquipmentText(room.equipment) }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-time"></i>
            <span>创建: {{ formatDate(room.createTime) }}</span>
          </div>
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
          <el-button
              type="info"
              size="small"
              @click="viewRoomDetail(room)"
          >
            查看详情
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && rooms.length === 0" description="暂无会议室数据"></el-empty>

    <!-- 会议室详情对话框 -->
    <el-dialog
        title="会议室详情"
        :visible.sync="roomDetailVisible"
        width="50%"
    >
      <div class="room-detail" v-if="selectedRoomDetail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>会议室名称：</label>
              <span>{{ selectedRoomDetail.roomName }}</span>
            </div>
            <div class="detail-item">
              <label>位置：</label>
              <span>{{ selectedRoomDetail.roomLocation }}</span>
            </div>
            <div class="detail-item">
              <label>容量：</label>
              <span>{{ selectedRoomDetail.capacity }} 人</span>
            </div>
            <div class="detail-item">
              <label>状态：</label>
              <el-tag :type="selectedRoomDetail.status === 1 ? 'success' : 'danger'">
                {{ selectedRoomDetail.status === 1 ? '可用' : '维护中' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="selectedRoomDetail.equipment">
          <h4>设备配置</h4>
          <div class="equipment-list">
            <el-tag
                v-for="(value, key) in parseEquipment(selectedRoomDetail.equipment)"
                :key="key"
                :type="value ? 'success' : 'info'"
                size="small"
                class="equipment-tag"
            >
              {{ getEquipmentName(key) }}: {{ value ? '有' : '无' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="roomDetailVisible = false">关闭</el-button>
        <el-button
            v-if="selectedRoomDetail && selectedRoomDetail.status === 1"
            type="primary"
            @click="bookFromDetail"
        >
          预订此会议室
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'BookingManage',
  data() {
    return {
      loading: false,
      showBookingDialog: false,
      roomDetailVisible: false,
      bookingLoading: false,
      rooms: [],
      selectedRoomDetail: null,
      bookingForm: {
        roomId: '',
        meetingTitle: '',
        meetingDesc: '',
        startTime: '',
        endTime: '',
        attendeeCount: 1,
        remindTime: 15
      },
      bookingRules: {
        roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
        meetingTitle: [
          { required: true, message: '请输入会议主题', trigger: 'blur' },
          { min: 2, max: 100, message: '会议主题长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        attendeeCount: [
          { required: true, message: '请输入参会人数', trigger: 'blur' },
          { type: 'number', min: 1, message: '参会人数至少为1人', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    // 可用的会议室（状态为1）
    availableRooms() {
      return this.rooms.filter(room => room.status === 1)
    },

    // 选中的会议室
    selectedRoom() {
      return this.rooms.find(room => room.id === this.bookingForm.roomId)
    },

    // 选中会议室的容量
    selectedRoomCapacity() {
      return this.selectedRoom ? this.selectedRoom.capacity : 50
    },

    // 开始时间选择器配置
    startTimeOptions() {
      return {
        disabledDate: (time) => {
          // 允许选择今天及以后的日期
          const today = new Date()
          today.setHours(0, 0, 0, 0)
          return time.getTime() < today.getTime()
        },
        disabledTime: (time) => {
          const now = new Date()
          const selectedDate = new Date(time)

          // 如果选择的是今天，则禁用已过去的小时
          if (selectedDate.toDateString() === now.toDateString()) {
            return {
              disabledHours: () => {
                const hours = []
                for (let i = 0; i < now.getHours(); i++) {
                  hours.push(i)
                }
                return hours
              },
              disabledMinutes: (hour) => {
                // 如果是当前小时，禁用已过去的分钟
                if (hour === now.getHours()) {
                  const minutes = []
                  for (let i = 0; i <= now.getMinutes(); i++) {
                    minutes.push(i)
                  }
                  return minutes
                }
                return []
              }
            }
          }
          return {}
        }
      }
    },

    // 结束时间选择器配置
    endTimeOptions() {
      return {
        disabledDate: (time) => {
          if (!this.bookingForm.startTime) return false

          // 允许选择开始时间当天及以后的日期
          const startDate = new Date(this.bookingForm.startTime)
          startDate.setHours(0, 0, 0, 0)
          return time.getTime() < startDate.getTime()
        },
        disabledTime: (time) => {
          if (!this.bookingForm.startTime) return {}

          const startTime = new Date(this.bookingForm.startTime)
          const selectedDate = new Date(time)

          // 如果选择的是开始时间的同一天
          if (selectedDate.toDateString() === startTime.toDateString()) {
            return {
              disabledHours: () => {
                const hours = []
                for (let i = 0; i < startTime.getHours(); i++) {
                  hours.push(i)
                }
                return hours
              },
              disabledMinutes: (hour) => {
                // 如果是开始时间的同一小时，禁用开始时间之前的分钟
                if (hour === startTime.getHours()) {
                  const minutes = []
                  for (let i = 0; i <= startTime.getMinutes(); i++) {
                    minutes.push(i)
                  }
                  return minutes
                }
                return []
              }
            }
          }
          return {}
        }
      }
    }
  },

  mounted() {
    this.loadRooms()
  },

  methods: {
    // 加载会议室列表
    async loadRooms() {
      this.loading = true
      try {
        const response = await this.$api.rooms.getAll()
        this.rooms = response.data || []
        console.log('获取会议室列表成功:', this.rooms)
      } catch (error) {
        console.error('获取会议室列表失败:', error)
        this.$message.error('获取会议室列表失败')
      } finally {
        this.loading = false
      }
    },

    // 开始时间改变时的处理
    onStartTimeChange(value) {
      // 如果结束时间早于或等于开始时间，自动调整结束时间
      if (value && this.bookingForm.endTime && new Date(this.bookingForm.endTime) <= new Date(value)) {
        const newEndTime = new Date(value)
        newEndTime.setHours(newEndTime.getHours() + 1) // 默认增加1小时
        this.bookingForm.endTime = newEndTime
      }
    },

    // 处理预订
    async handleBooking() {
      try {
        const valid = await this.$refs.bookingForm.validate()
        if (!valid) return

        // 验证时间
        if (new Date(this.bookingForm.endTime) <= new Date(this.bookingForm.startTime)) {
          this.$message.error('结束时间必须晚于开始时间')
          return
        }

        // 验证人数
        if (this.selectedRoom && this.bookingForm.attendeeCount > this.selectedRoom.capacity) {
          this.$message.error(`参会人数不能超过会议室容量（${this.selectedRoom.capacity}人）`)
          return
        }

        // 验证时间间隔（至少15分钟）
        const timeDiff = new Date(this.bookingForm.endTime) - new Date(this.bookingForm.startTime)
        if (timeDiff < 15 * 60 * 1000) {
          this.$message.error('会议时长至少为15分钟')
          return
        }

        // 验证会议时长不超过8小时
        if (timeDiff > 8 * 60 * 60 * 1000) {
          this.$message.error('会议时长不能超过8小时')
          return
        }

        this.bookingLoading = true

        // 格式化时间为ISO字符串
        const bookingData = {
          ...this.bookingForm,
          startTime: new Date(this.bookingForm.startTime).toISOString(),
          endTime: new Date(this.bookingForm.endTime).toISOString()
        }

        await this.$api.bookings.book(bookingData)
        this.$message.success('预订成功！')
        this.showBookingDialog = false
        this.resetBookingForm()
      } catch (error) {
        console.error('预订失败:', error)
        this.$message.error(error.response?.data?.message || '预订失败，请稍后重试')
      } finally {
        this.bookingLoading = false
      }
    },

    // 重置预订表单
    resetBookingForm() {
      if (this.$refs.bookingForm) {
        this.$refs.bookingForm.resetFields()
      }
      this.bookingForm = {
        roomId: '',
        meetingTitle: '',
        meetingDesc: '',
        startTime: '',
        endTime: '',
        attendeeCount: 1,
        remindTime: 15
      }
    },

    // 快速预订
    quickBook(room) {
      this.bookingForm.roomId = room.id
      // 设置默认时间（当前时间后1小时开始，持续1小时）
      const now = new Date()
      const startTime = new Date(now.getTime() + 60 * 60 * 1000) // 1小时后
      const endTime = new Date(startTime.getTime() + 60 * 60 * 1000) // 再1小时后

      this.bookingForm.startTime = startTime
      this.bookingForm.endTime = endTime
      this.showBookingDialog = true
    },

    // 会议室选择改变
    onRoomChange(roomId) {
      const room = this.rooms.find(r => r.id === roomId)
      if (room && this.bookingForm.attendeeCount > room.capacity) {
        this.bookingForm.attendeeCount = room.capacity
        this.$message.warning(`参会人数已调整为会议室最大容量：${room.capacity}人`)
      }
    },

    // 查看会议室详情
    viewRoomDetail(room) {
      this.selectedRoomDetail = room
      this.roomDetailVisible = true
    },

    // 从详情页预订
    bookFromDetail() {
      this.roomDetailVisible = false
      this.quickBook(this.selectedRoomDetail)
    },

    // 解析设备配置
    parseEquipment(equipmentStr) {
      try {
        return JSON.parse(equipmentStr || '{}')
      } catch (e) {
        return {}
      }
    },

    // 获取设备文本
    getEquipmentText(equipmentStr) {
      try {
        const equipment = JSON.parse(equipmentStr || '{}')
        const availableEquipment = []

        if (equipment.projector) availableEquipment.push('投影仪')
        if (equipment.whiteboard) availableEquipment.push('白板')
        if (equipment.videoConference) availableEquipment.push('视频会议')
        if (equipment.airConditioner) availableEquipment.push('空调')
        if (equipment.microphone) availableEquipment.push('麦克风')
        if (equipment.wifi) availableEquipment.push('WiFi')

        return availableEquipment.length > 0 ? availableEquipment.join('、') : '基础设备'
      } catch (e) {
        return '基础设备'
      }
    },

    // 获取设备名称
    getEquipmentName(key) {
      const names = {
        projector: '投影仪',
        whiteboard: '白板',
        videoConference: '视频会议',
        airConditioner: '空调',
        microphone: '麦克风',
        wifi: 'WiFi'
      }
      return names[key] || key
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

.header-actions {
  display: flex;
  gap: 10px;
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
  border-left: 4px solid #409eff;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.room-card.disabled {
  opacity: 0.7;
  border-left-color: #f56c6c;
  background: #fafafa;
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
  width: 16px;
}

.room-actions {
  display: flex;
  gap: 10px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

/* 表单样式 */
.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 详情对话框样式 */
.room-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
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
}

.equipment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.equipment-tag {
  margin-bottom: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .room-grid {
    grid-template-columns: 1fr;
  }

  .room-actions {
    flex-direction: column;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>