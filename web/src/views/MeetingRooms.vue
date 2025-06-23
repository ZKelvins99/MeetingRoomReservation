<template>
  <div class="meeting-rooms">
    <div class="page-header">
      <h2>会议室管理</h2>
      <div class="header-actions">
        <el-button
            type="primary"
            icon="el-icon-refresh"
            @click="loadRooms"
            :loading="loading"
        >
          刷新
        </el-button>
        <el-button
            type="primary"
            icon="el-icon-plus"
            @click="showAddDialog"
        >
          添加会议室
        </el-button>
      </div>
    </div>

    <div class="rooms-grid" v-loading="loading">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="room-card"
          :class="{ 'disabled': room.status === 0 }"
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
          <div class="info-item" v-if="room.equipment">
            <i class="el-icon-monitor"></i>
            <span>设备：{{ getEquipmentText(room.equipment) }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-time"></i>
            <span>创建时间：{{ formatDateTime(room.createTime) }}</span>
          </div>
        </div>

        <div class="room-footer">
          <el-button size="small" @click="viewRoom(room)">
            查看详情
          </el-button>
          <el-button size="small" @click="editRoom(room)">
            编辑
          </el-button>
          <el-button
              size="small"
              type="danger"
              @click="deleteRoom(room)"
              :loading="deleteLoading === room.id"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && rooms.length === 0" description="暂无会议室数据"></el-empty>

    <!-- 添加/编辑对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="50%"
        @close="closeDialog"
    >
      <el-form
          ref="roomForm"
          :model="roomForm"
          :rules="formRules"
          label-width="100px"
      >
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="roomForm.roomName" placeholder="请输入会议室名称"></el-input>
        </el-form-item>

        <el-form-item label="位置" prop="roomLocation">
          <el-input v-model="roomForm.roomLocation" placeholder="请输入会议室位置"></el-input>
        </el-form-item>

        <el-form-item label="容量" prop="capacity">
          <el-input-number
              v-model="roomForm.capacity"
              :min="1"
              :max="200"
              placeholder="请输入会议室容量"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roomForm.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">维护中</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="设备配置">
          <div class="equipment-config">
            <el-checkbox v-model="equipment.projector">投影仪</el-checkbox>
            <el-checkbox v-model="equipment.whiteboard">白板</el-checkbox>
            <el-checkbox v-model="equipment.videoConference">视频会议</el-checkbox>
            <el-checkbox v-model="equipment.airConditioner">空调</el-checkbox>
            <el-checkbox v-model="equipment.microphone">麦克风</el-checkbox>
            <el-checkbox v-model="equipment.wifi">WiFi</el-checkbox>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="saveRoom"
            :loading="saveLoading"
        >
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
        title="会议室详情"
        :visible.sync="detailDialogVisible"
        width="50%"
    >
      <div class="room-detail" v-if="selectedRoom">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>会议室名称：</label>
              <span>{{ selectedRoom.roomName }}</span>
            </div>
            <div class="detail-item">
              <label>位置：</label>
              <span>{{ selectedRoom.roomLocation }}</span>
            </div>
            <div class="detail-item">
              <label>容量：</label>
              <span>{{ selectedRoom.capacity }} 人</span>
            </div>
            <div class="detail-item">
              <label>状态：</label>
              <el-tag :type="selectedRoom.status === 1 ? 'success' : 'danger'">
                {{ selectedRoom.status === 1 ? '可用' : '维护中' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="selectedRoom.equipment">
          <h4>设备配置</h4>
          <div class="equipment-list">
            <el-tag
                v-for="(value, key) in parseEquipment(selectedRoom.equipment)"
                :key="key"
                :type="value ? 'success' : 'info'"
                size="small"
                class="equipment-tag"
            >
              {{ getEquipmentName(key) }}: {{ value ? '有' : '无' }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h4>时间信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ formatDateTime(selectedRoom.createTime) }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间：</label>
              <span>{{ formatDateTime(selectedRoom.updateTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editRoomFromDetail">
          编辑
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MeetingRooms',
  data() {
    return {
      rooms: [],
      loading: false,
      saveLoading: false,
      deleteLoading: null,

      // 对话框相关
      dialogVisible: false,
      detailDialogVisible: false,
      isEdit: false,
      selectedRoom: null,

      // 表单数据
      roomForm: {
        id: null,
        roomName: '',
        roomLocation: '',
        capacity: 1,
        status: 1,
        equipment: ''
      },

      // 设备配置
      equipment: {
        projector: false,
        whiteboard: false,
        videoConference: false,
        airConditioner: false,
        microphone: false,
        wifi: false
      },

      // 表单验证规则
      formRules: {
        roomName: [
          { required: true, message: '请输入会议室名称', trigger: 'blur' },
          { min: 2, max: 50, message: '会议室名称长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        roomLocation: [
          { required: true, message: '请输入会议室位置', trigger: 'blur' },
          { min: 2, max: 100, message: '位置长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        capacity: [
          { required: true, message: '请输入会议室容量', trigger: 'blur' },
          { type: 'number', min: 1, max: 200, message: '容量必须在 1 到 200 之间', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择会议室状态', trigger: 'change' }
        ]
      }
    }
  },

  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑会议室' : '添加会议室'
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

    // 显示添加对话框
    showAddDialog() {
      this.isEdit = false
      this.resetForm()
      this.dialogVisible = true
    },

    // 查看详情
    viewRoom(room) {
      this.selectedRoom = room
      this.detailDialogVisible = true
    },

    // 编辑会议室
    editRoom(room) {
      this.isEdit = true
      this.roomForm = { ...room }

      // 解析设备配置
      if (room.equipment) {
        try {
          const equipmentData = JSON.parse(room.equipment)
          this.equipment = {
            projector: equipmentData.projector || false,
            whiteboard: equipmentData.whiteboard || false,
            videoConference: equipmentData.videoConference || false,
            airConditioner: equipmentData.airConditioner || false,
            microphone: equipmentData.microphone || false,
            wifi: equipmentData.wifi || false
          }
        } catch (e) {
          console.error('解析设备配置失败:', e)
          this.resetEquipment()
        }
      } else {
        this.resetEquipment()
      }

      this.dialogVisible = true
    },

    // 从详情页编辑
    editRoomFromDetail() {
      this.detailDialogVisible = false
      this.editRoom(this.selectedRoom)
    },

    // 保存会议室
    async saveRoom() {
      try {
        const valid = await this.$refs.roomForm.validate()
        if (!valid) return

        this.saveLoading = true

        // 构建设备配置JSON
        this.roomForm.equipment = JSON.stringify(this.equipment)

        if (this.isEdit) {
          await this.$api.rooms.update(this.roomForm.id, this.roomForm)
          this.$message.success('更新会议室成功')
        } else {
          await this.$api.rooms.create(this.roomForm)
          this.$message.success('添加会议室成功')
        }

        this.dialogVisible = false
        await this.loadRooms()
      } catch (error) {
        console.error('保存会议室失败:', error)
        this.$message.error('保存会议室失败')
      } finally {
        this.saveLoading = false
      }
    },

    // 删除会议室
    async deleteRoom(room) {
      try {
        await this.$confirm(`确定要删除会议室"${room.roomName}"吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.deleteLoading = room.id
        await this.$api.rooms.delete(room.id)
        this.$message.success('删除会议室成功')
        await this.loadRooms()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除会议室失败:', error)
          this.$message.error('删除会议室失败')
        }
      } finally {
        this.deleteLoading = null
      }
    },

    // 关闭对话框
    closeDialog() {
      this.resetForm()
      this.dialogVisible = false
    },

    // 重置表单
    resetForm() {
      this.roomForm = {
        id: null,
        roomName: '',
        roomLocation: '',
        capacity: 1,
        status: 1,
        equipment: ''
      }
      this.resetEquipment()
      this.$nextTick(() => {
        if (this.$refs.roomForm) {
          this.$refs.roomForm.clearValidate()
        }
      })
    },

    // 重置设备配置
    resetEquipment() {
      this.equipment = {
        projector: false,
        whiteboard: false,
        videoConference: false,
        airConditioner: false,
        microphone: false,
        wifi: false
      }
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

        return availableEquipment.length > 0 ? availableEquipment.join('、') : '无'
      } catch (e) {
        return '无'
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
.meeting-rooms {
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

.header-actions {
  display: flex;
  gap: 10px;
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
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.room-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.room-card.disabled {
  opacity: 0.7;
  background: #fafafa;
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
  width: 16px;
}

.room-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

/* 设备配置样式 */
.equipment-config {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.equipment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.equipment-tag {
  margin-bottom: 5px;
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

  .rooms-grid {
    grid-template-columns: 1fr;
  }

  .equipment-config {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>