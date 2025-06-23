<template>
  <div class="checkin-manage">
    <div class="page-header">
      <h2>签到管理</h2>
      <div class="header-actions">
        <el-button
            type="default"
            icon="el-icon-refresh"
            @click="loadCurrentMeetings"
            :loading="loading"
        >
          刷新
        </el-button>
        <el-button
            type="primary"
            icon="el-icon-qrcode"
            @click="showQRCodeDialog"
        >
          扫码签到
        </el-button>
        <el-button
            type="success"
            icon="el-icon-edit"
            @click="showManualDialog"
        >
          手动签到
        </el-button>
      </div>
    </div>

    <!-- 当前可签到的会议 -->
    <el-card class="current-meetings" v-loading="loading">
      <div slot="header">
        <h3>当前可签到的会议</h3>
        <span class="meeting-count">共 {{ currentMeetings.length }} 个进行中的会议</span>
      </div>

      <div class="meeting-grid" v-if="currentMeetings.length > 0">
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
            <div class="info-item">
              <i class="el-icon-office-building"></i>
              <span>{{ meeting.roomName }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span>{{ formatTime(meeting.startTime) }} - {{ formatTime(meeting.endTime) }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span>预计参会 {{ meeting.attendeeCount }} 人</span>
            </div>
            <div class="info-item" v-if="meeting.checkinStats">
              <i class="el-icon-circle-check"></i>
              <span>已签到 {{ meeting.checkinStats.checkedInCount || 0 }} 人</span>
            </div>
          </div>
          <div class="meeting-actions">
            <el-button
                type="primary"
                size="small"
                @click="quickCheckin(meeting)"
                :loading="checkinLoading === meeting.id"
            >
              立即签到
            </el-button>
            <el-button
                type="info"
                size="small"
                @click="viewCheckinStats(meeting)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无进行中的会议"></el-empty>
    </el-card>

    <!-- 签到历史 -->
    <el-card class="checkin-history">
      <div slot="header">
        <h3>我的签到历史</h3>
      </div>

      <el-table
          :data="checkinHistory"
          v-loading="historyLoading"
          empty-text="暂无签到记录"
      >
        <el-table-column label="会议主题" prop="meetingTitle" min-width="200"></el-table-column>
        <el-table-column label="签到时间" prop="checkinTime" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.checkinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="签到方式" prop="checkinType" width="120">
          <template slot-scope="scope">
            <el-tag
                size="small"
                :type="getCheckinTypeTagType(scope.row.checkinType)"
            >
              {{ getCheckinTypeText(scope.row.checkinType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="checkinStatus" width="100">
          <template slot-scope="scope">
            <el-tag
                size="small"
                :type="scope.row.checkinStatus === 'PRESENT' ? 'success' : 'danger'"
            >
              {{ scope.row.checkinStatus === 'PRESENT' ? '已签到' : '缺席' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 扫码签到对话框 -->
    <el-dialog
        title="扫码签到"
        :visible.sync="qrCodeDialogVisible"
        width="400px"
    >
      <div class="qr-code-section">
        <p class="dialog-tip">请使用手机扫描下方二维码进行签到</p>
        <div class="qr-code-placeholder">
          <i class="el-icon-qrcode"></i>
          <p>二维码区域</p>
          <p class="qr-tip">（实际项目中可集成二维码生成库）</p>
        </div>

        <!-- 手动输入会议ID -->
        <el-divider>或</el-divider>
        <el-form :model="qrCodeForm" label-width="80px">
          <el-form-item label="会议ID">
            <el-select
                v-model="qrCodeForm.bookingId"
                placeholder="选择要签到的会议"
                style="width: 100%"
            >
              <el-option
                  v-for="meeting in currentMeetings"
                  :key="meeting.id"
                  :label="meeting.meetingTitle"
                  :value="meeting.id"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer">
        <el-button @click="qrCodeDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleQRCodeCheckin"
            :loading="qrCheckinLoading"
            :disabled="!qrCodeForm.bookingId"
        >
          签到
        </el-button>
      </div>
    </el-dialog>

    <!-- 手动签到对话框 -->
    <el-dialog
        title="手动签到"
        :visible.sync="manualDialogVisible"
        width="500px"
    >
      <el-form
          :model="manualForm"
          :rules="manualRules"
          ref="manualForm"
          label-width="100px"
      >
        <el-form-item label="选择会议" prop="bookingId">
          <el-select
              v-model="manualForm.bookingId"
              placeholder="请选择要签到的会议"
              style="width: 100%"
          >
            <el-option
                v-for="meeting in currentMeetings"
                :key="meeting.id"
                :label="meeting.meetingTitle"
                :value="meeting.id"
            >
              <span>{{ meeting.meetingTitle }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ meeting.roomName }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="工号" prop="userCode">
          <el-input
              v-model="manualForm.userCode"
              placeholder="请输入您的工号"
              maxlength="20"
          ></el-input>
          <div class="form-hint">请输入您的员工工号进行签到验证</div>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleManualCheckin"
            :loading="manualCheckinLoading"
        >
          确认签到
        </el-button>
      </div>
    </el-dialog>

    <!-- 签到统计对话框 -->
    <el-dialog
        title="签到统计"
        :visible.sync="statsDialogVisible"
        width="60%"
    >
      <div class="stats-content" v-if="selectedMeeting">
        <div class="stats-header">
          <h4>{{ selectedMeeting.meetingTitle }}</h4>
          <div class="stats-summary">
            <el-tag type="success" size="medium">
              已签到: {{ checkinStats.checkedInCount || 0 }}人
            </el-tag>
            <el-tag type="info" size="medium">
              预计参会: {{ selectedMeeting.attendeeCount }}人
            </el-tag>
          </div>
        </div>

        <el-table
            :data="checkinList"
            v-loading="statsLoading"
            max-height="400"
        >
          <el-table-column label="姓名" prop="realName" width="120"></el-table-column>
          <el-table-column label="签到时间" prop="checkinTime" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.checkinTime) }}
            </template>
          </el-table-column>
          <el-table-column label="签到方式" prop="checkinType" width="120">
            <template slot-scope="scope">
              <el-tag
                  size="small"
                  :type="getCheckinTypeTagType(scope.row.checkinType)"
              >
                {{ getCheckinTypeText(scope.row.checkinType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="checkinStatus" width="100">
            <template slot-scope="scope">
              <el-tag
                  size="small"
                  :type="scope.row.checkinStatus === 'PRESENT' ? 'success' : 'danger'"
              >
                {{ scope.row.checkinStatus === 'PRESENT' ? '已签到' : '缺席' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer">
        <el-button @click="statsDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CheckinManage',
  data() {
    return {
      loading: false,
      historyLoading: false,
      statsLoading: false,
      checkinLoading: null,
      qrCheckinLoading: false,
      manualCheckinLoading: false,

      // 对话框状态
      qrCodeDialogVisible: false,
      manualDialogVisible: false,
      statsDialogVisible: false,

      // 数据
      currentMeetings: [],
      checkinHistory: [],
      checkinList: [],
      checkinStats: {},
      selectedMeeting: null,

      // 表单数据
      qrCodeForm: {
        bookingId: ''
      },
      manualForm: {
        bookingId: '',
        userCode: ''
      },

      // 表单验证规则
      manualRules: {
        bookingId: [
          { required: true, message: '请选择要签到的会议', trigger: 'change' }
        ],
        userCode: [
          { required: true, message: '请输入工号', trigger: 'blur' },
          { min: 3, max: 20, message: '工号长度在 3 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },

  mounted() {
    this.loadCurrentMeetings()
    this.loadCheckinHistory()
  },

  methods: {
    // 加载当前可签到的会议
    async loadCurrentMeetings() {
      this.loading = true
      try {
        // 获取我的预订列表，筛选出进行中的会议
        const response = await this.$api.bookings.getMy()
        const allBookings = response.data.records || []

        const now = new Date()
        this.currentMeetings = []

        // 筛选进行中的会议并获取签到统计
        for (const booking of allBookings) {
          const startTime = new Date(booking.startTime)
          const endTime = new Date(booking.endTime)

          // 判断会议是否正在进行中
          if (now >= startTime && now <= endTime && booking.bookingStatus === 'BOOKED') {
            // 获取签到统计
            try {
              const statsResponse = await this.$api.checkin.getStatistics(booking.id)
              booking.checkinStats = statsResponse.data
            } catch (error) {
              console.warn('获取签到统计失败:', error)
              booking.checkinStats = { checkedInCount: 0 }
            }

            this.currentMeetings.push(booking)
          }
        }

        console.log('当前可签到会议:', this.currentMeetings)
      } catch (error) {
        console.error('获取当前会议失败:', error)
        this.$message.error('获取当前会议失败')
      } finally {
        this.loading = false
      }
    },

    // 加载签到历史
    async loadCheckinHistory() {
      this.historyLoading = true
      try {
        // 这里需要根据实际API调整，可能需要先获取用户的预订，再获取签到记录
        // 暂时使用模拟数据
        this.checkinHistory = [
          {
            id: 1,
            meetingTitle: '周例会',
            checkinTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
            checkinType: 'QR_CODE',
            checkinStatus: 'PRESENT'
          },
          {
            id: 2,
            meetingTitle: '产品评审',
            checkinTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
            checkinType: 'MANUAL',
            checkinStatus: 'PRESENT'
          }
        ]
      } catch (error) {
        console.error('获取签到历史失败:', error)
        this.$message.error('获取签到历史失败')
      } finally {
        this.historyLoading = false
      }
    },

    // 快速签到
    async quickCheckin(meeting) {
      try {
        await this.$confirm(`确定要签到会议"${meeting.meetingTitle}"吗？`, '确认签到', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })

        this.checkinLoading = meeting.id

        const checkinData = {
          bookingId: meeting.id,
          checkinType: 'MANUAL'
        }

        await this.$api.checkin.manual(checkinData)
        this.$message.success('签到成功！')

        // 刷新当前会议列表
        await this.loadCurrentMeetings()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('签到失败:', error)
          this.$message.error(error.response?.data?.message || '签到失败')
        }
      } finally {
        this.checkinLoading = null
      }
    },

    // 显示二维码对话框
    showQRCodeDialog() {
      if (this.currentMeetings.length === 0) {
        this.$message.warning('当前没有进行中的会议')
        return
      }
      this.qrCodeForm.bookingId = ''
      this.qrCodeDialogVisible = true
    },

    // 处理二维码签到
    async handleQRCodeCheckin() {
      if (!this.qrCodeForm.bookingId) {
        this.$message.error('请选择要签到的会议')
        return
      }

      this.qrCheckinLoading = true
      try {
        const checkinData = {
          bookingId: this.qrCodeForm.bookingId,
          checkinType: 'QR_CODE'
        }

        await this.$api.checkin.scan(checkinData)
        this.$message.success('扫码签到成功！')
        this.qrCodeDialogVisible = false

        // 刷新数据
        await this.loadCurrentMeetings()
        await this.loadCheckinHistory()
      } catch (error) {
        console.error('扫码签到失败:', error)
        this.$message.error(error.response?.data?.message || '扫码签到失败')
      } finally {
        this.qrCheckinLoading = false
      }
    },

    // 显示手动签到对话框
    showManualDialog() {
      if (this.currentMeetings.length === 0) {
        this.$message.warning('当前没有进行中的会议')
        return
      }
      this.resetManualForm()
      this.manualDialogVisible = true
    },

    // 处理手动签到
    async handleManualCheckin() {
      try {
        const valid = await this.$refs.manualForm.validate()
        if (!valid) return

        this.manualCheckinLoading = true

        const checkinData = {
          bookingId: this.manualForm.bookingId,
          checkinType: 'MANUAL',
          userCode: this.manualForm.userCode
        }

        await this.$api.checkin.manual(checkinData)
        this.$message.success('手动签到成功！')
        this.manualDialogVisible = false

        // 刷新数据
        await this.loadCurrentMeetings()
        await this.loadCheckinHistory()
      } catch (error) {
        console.error('手动签到失败:', error)
        this.$message.error(error.response?.data?.message || '手动签到失败')
      } finally {
        this.manualCheckinLoading = false
      }
    },

    // 查看签到统计
    async viewCheckinStats(meeting) {
      this.selectedMeeting = meeting
      this.statsDialogVisible = true
      this.statsLoading = true

      try {
        // 获取签到统计
        const statsResponse = await this.$api.checkin.getStatistics(meeting.id)
        this.checkinStats = statsResponse.data

        // 获取签到列表
        const listResponse = await this.$api.checkin.getList({ bookingId: meeting.id })
        this.checkinList = listResponse.data.records || []
      } catch (error) {
        console.error('获取签到统计失败:', error)
        this.$message.error('获取签到统计失败')
      } finally {
        this.statsLoading = false
      }
    },

    // 重置手动签到表单
    resetManualForm() {
      this.manualForm = {
        bookingId: '',
        userCode: ''
      }
      if (this.$refs.manualForm) {
        this.$refs.manualForm.clearValidate()
      }
    },

    // 获取签到方式标签类型
    getCheckinTypeTagType(type) {
      switch (type) {
        case 'QR_CODE':
          return 'primary'
        case 'MANUAL':
          return 'success'
        case 'NFC':
          return 'warning'
        default:
          return 'info'
      }
    },

    // 获取签到方式文本
    getCheckinTypeText(type) {
      switch (type) {
        case 'QR_CODE':
          return '扫码'
        case 'MANUAL':
          return '手动'
        case 'NFC':
          return 'NFC'
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
.checkin-manage {
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

.current-meetings {
  margin-bottom: 20px;
}

.current-meetings .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-meetings .el-card__header h3 {
  margin: 0;
  color: #303133;
}

.meeting-count {
  color: #909399;
  font-size: 14px;
}

.meeting-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.meeting-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  border-left: 4px solid #67c23a;
}

.meeting-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  transform: translateY(-2px);
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
  font-size: 16px;
  font-weight: 600;
}

.meeting-info {
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

.meeting-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.checkin-history {
  margin-top: 20px;
}

.checkin-history .el-card__header h3 {
  margin: 0;
  color: #303133;
}

/* 对话框样式 */
.qr-code-section {
  text-align: center;
}

.dialog-tip {
  color: #606266;
  margin-bottom: 20px;
}

.qr-code-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #909399;
}

.qr-code-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

.qr-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin: 5px 0 0 0;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 统计对话框样式 */
.stats-content {
  max-height: 60vh;
  overflow-y: auto;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.stats-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.stats-summary {
  display: flex;
  gap: 10px;
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

  .meeting-grid {
    grid-template-columns: 1fr;
  }

  .meeting-actions {
    flex-direction: column;
  }

  .stats-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>