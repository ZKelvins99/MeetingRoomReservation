<template>
  <div class="checkin-scan">
    <div class="scan-header">
      <h1>会议签到</h1>
      <p>请扫描二维码或输入签到码进行签到</p>
    </div>

    <div class="scan-container">
      <!-- 扫码区域 -->
      <div class="scan-section">
        <div class="camera-container">
          <video ref="video" class="camera-video" autoplay muted></video>
          <div class="scan-frame">
            <div class="scan-line"></div>
            <div class="corner top-left"></div>
            <div class="corner top-right"></div>
            <div class="corner bottom-left"></div>
            <div class="corner bottom-right"></div>
          </div>
        </div>
        
        <div class="scan-controls">
          <el-button 
            type="primary" 
            @click="startCamera" 
            :disabled="cameraActive"
            icon="el-icon-video-camera"
          >
            启动摄像头
          </el-button>
          <el-button 
            @click="stopCamera" 
            :disabled="!cameraActive"
            icon="el-icon-video-pause"
          >
            停止扫描
          </el-button>
        </div>
        
        <p class="scan-tips">将二维码对准扫描框进行签到</p>
      </div>

      <!-- 手动输入区域 -->
      <div class="manual-section">
        <el-divider>或手动输入</el-divider>
        
        <el-form :model="manualForm" :rules="manualRules" ref="manualForm">
          <el-form-item prop="code">
            <el-input
              v-model="manualForm.code"
              placeholder="请输入签到码"
              size="large"
              clearable
              @keyup.enter.native="handleManualCheckin"
            >
              <i slot="prefix" class="el-icon-key"></i>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="success" 
              size="large"
              @click="handleManualCheckin"
              :loading="checkinLoading"
              style="width: 100%"
            >
              确认签到
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 签到结果对话框 -->
    <el-dialog
      :title="checkinResult.success ? '签到成功' : '签到失败'"
      :visible.sync="showResultDialog"
      width="500px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="result-content">
        <div class="result-icon">
          <i 
            :class="checkinResult.success ? 'el-icon-success' : 'el-icon-error'"
            :style="{ color: checkinResult.success ? '#67c23a' : '#f56c6c' }"
          ></i>
        </div>
        
        <div class="result-info">
          <h3>{{ checkinResult.message }}</h3>
          
          <div v-if="checkinResult.success && checkinResult.meetingInfo" class="meeting-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="会议主题">
                {{ checkinResult.meetingInfo.meetingTitle }}
              </el-descriptions-item>
              <el-descriptions-item label="会议室">
                {{ checkinResult.meetingInfo.roomName }}
              </el-descriptions-item>
              <el-descriptions-item label="签到时间">
                {{ checkinResult.checkinTime | formatDateTime }}
              </el-descriptions-item>
              <el-descriptions-item label="会议时间">
                {{ checkinResult.meetingInfo.startTime | formatTime }} - 
                {{ checkinResult.meetingInfo.endTime | formatTime }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
      
      <div slot="footer">
        <el-button @click="showResultDialog = false">关闭</el-button>
        <el-button 
          v-if="checkinResult.success"
          type="primary" 
          @click="continueScanning"
        >
          继续扫码
        </el-button>
      </div>
    </el-dialog>

    <!-- 会议信息预览 -->
    <div v-if="scannedMeeting" class="meeting-preview">
      <el-card>
        <div slot="header">
          <h3>会议信息预览</h3>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会议主题">{{ scannedMeeting.meetingTitle }}</el-descriptions-item>
          <el-descriptions-item label="会议室">{{ scannedMeeting.roomName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ scannedMeeting.startTime | formatDateTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ scannedMeeting.endTime | formatDateTime }}</el-descriptions-item>
          <el-descriptions-item label="预订人">{{ scannedMeeting.realName }}</el-descriptions-item>
          <el-descriptions-item label="参会人数">{{ scannedMeeting.attendeeCount }}人</el-descriptions-item>
        </el-descriptions>
        
        <div class="preview-actions">
          <el-button @click="scannedMeeting = null">取消</el-button>
          <el-button type="primary" @click="confirmCheckin" :loading="checkinLoading">
            确认签到
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CheckinScan',
  data() {
    return {
      cameraActive: false,
      stream: null,
      checkinLoading: false,
      
      // 手动输入表单
      manualForm: {
        code: ''
      },
      manualRules: {
        code: [
          { required: true, message: '请输入签到码', trigger: 'blur' }
        ]
      },
      
      // 签到结果
      showResultDialog: false,
      checkinResult: {
        success: false,
        message: '',
        meetingInfo: null,
        checkinTime: null
      },
      
      // 扫描到的会议信息
      scannedMeeting: null
    }
  },
  mounted() {
    // 检查URL参数，如果有bookingId则自动开始签到流程
    const bookingId = this.$route.query.bookingId
    if (bookingId) {
      this.autoCheckin(bookingId)
    }
  },
  beforeDestroy() {
    this.stopCamera()
  },
  methods: {
    async startCamera() {
      try {
        this.stream = await navigator.mediaDevices.getUserMedia({
          video: { 
            facingMode: 'environment',
            width: { ideal: 1280 },
            height: { ideal: 720 }
          }
        })
        
        this.$refs.video.srcObject = this.stream
        this.cameraActive = true
        
        // 开始检测二维码
        this.startQRCodeDetection()
        
        this.$message.success('摄像头启动成功，请对准二维码')
      } catch (error) {
        this.$message.error('无法访问摄像头，请检查权限设置')
        console.error('摄像头启动失败:', error)
      }
    },
    
    stopCamera() {
      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop())
        this.stream = null
      }
      this.cameraActive = false
    },
    
    startQRCodeDetection() {
      // 这里应该集成二维码检测库，如 jsQR
      // 简化实现：模拟二维码检测
      const detectInterval = setInterval(() => {
        if (!this.cameraActive) {
          clearInterval(detectInterval)
          return
        }
        
        // 模拟检测到二维码（实际应该使用图像处理库）
        // 这里只是示例，实际项目中需要集成 jsQR 或其他库
      }, 100)
    },
    
    async handleManualCheckin() {
      this.$refs.manualForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const bookingId = this.parseQRCode(this.manualForm.code)
          await this.performCheckin(bookingId)
        } catch (error) {
          this.showCheckinResult(false, error.message)
        }
      })
    },
    
    parseQRCode(code) {
      try {
        // 尝试解析URL格式的二维码
        const url = new URL(code)
        const bookingId = url.searchParams.get('bookingId')
        
        if (bookingId) {
          return parseInt(bookingId)
        }
        
        // 尝试直接解析为数字
        const numericId = parseInt(code)
        if (!isNaN(numericId) && numericId > 0) {
          return numericId
        }
        
        throw new Error('无效的签到码格式')
      } catch (error) {
        // 如果不是有效URL，尝试其他解析方式
        const match = code.match(/bookingId[=:](\d+)/)
        if (match) {
          return parseInt(match[1])
        }
        
        throw new Error('无法识别的签到码')
      }
    },
    
    async autoCheckin(bookingId) {
      try {
        await this.performCheckin(parseInt(bookingId))
      } catch (error) {
        this.showCheckinResult(false, error.message)
      }
    },
    
    async performCheckin(bookingId) {
      this.checkinLoading = true
      
      try {
        // 先获取会议信息进行确认
        const meetingResponse = await this.$api.bookings.getById(bookingId)
        this.scannedMeeting = meetingResponse.data
        
        // 如果不是从URL自动跳转的，显示确认对话框
        if (!this.$route.query.bookingId) {
          return // 等待用户确认
        }
        
        // 直接进行签到
        await this.confirmCheckin()
        
      } catch (error) {
        this.showCheckinResult(false, '签到失败：' + error.message)
      } finally {
        this.checkinLoading = false
      }
    },
    
    async confirmCheckin() {
      if (!this.scannedMeeting) return
      
      this.checkinLoading = true
      
      try {
        const checkinData = {
          bookingId: this.scannedMeeting.id,
          checkinType: 'QR_CODE'
        }
        
        await this.$api.checkin.scan(checkinData)
        
        this.showCheckinResult(true, '签到成功！', {
          meetingInfo: this.scannedMeeting,
          checkinTime: new Date()
        })
        
        this.scannedMeeting = null
        this.manualForm.code = ''
        
      } catch (error) {
        this.showCheckinResult(false, '签到失败：' + error.message)
      } finally {
        this.checkinLoading = false
      }
    },
    
    showCheckinResult(success, message, extraData = {}) {
      this.checkinResult = {
        success,
        message,
        ...extraData
      }
      this.showResultDialog = true
    },
    
    continueScanning() {
      this.showResultDialog = false
      this.manualForm.code = ''
      
      // 如果摄像头未启动，自动启动
      if (!this.cameraActive) {
        this.startCamera()
      }
    }
  }
}
</script>

<style scoped>
.checkin-scan {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.scan-header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.scan-header h1 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 600;
}

.scan-header p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.scan-container {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 500px;
}

.scan-section {
  text-align: center;
  margin-bottom: 30px;
}

.camera-container {
  position: relative;
  width: 300px;
  height: 300px;
  margin: 0 auto 20px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border: 2px solid transparent;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #409eff;
  animation: scanLine 2s linear infinite;
}

@keyframes scanLine {
  0% { top: 0; }
  100% { top: calc(100% - 2px); }
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #409eff;
}

.corner.top-left {
  top: -2px;
  left: -2px;
  border-right: none;
  border-bottom: none;
}

.corner.top-right {
  top: -2px;
  right: -2px;
  border-left: none;
  border-bottom: none;
}

.corner.bottom-left {
  bottom: -2px;
  left: -2px;
  border-right: none;
  border-top: none;
}

.corner.bottom-right {
  bottom: -2px;
  right: -2px;
  border-left: none;
  border-top: none;
}

.scan-controls {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 15px;
}

.scan-tips {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.manual-section {
  margin-top: 20px;
}

.result-content {
  text-align: center;
  padding: 20px;
}

.result-icon {
  margin-bottom: 20px;
}

.result-icon i {
  font-size: 48px;
}

.result-info h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

.meeting-info {
  text-align: left;
}

.meeting-preview {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2000;
  width: 90%;
  max-width: 600px;
}

.preview-actions {
  margin-top: 20px;
  text-align: right;
}

.preview-actions .el-button {
  margin-left: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-scan {
    padding: 10px;
  }
  
  .scan-header h1 {
    font-size: 24px;
  }
  
  .scan-container {
    padding: 20px;
  }
  
  .camera-container {
    width: 250px;
    height: 250px;
  }
  
  .scan-frame {
    width: 150px;
    height: 150px;
  }
  
  .scan-controls {
    flex-direction: column;
  }
  
  .scan-controls .el-button {
    margin-bottom: 10px;
  }
}
</style>