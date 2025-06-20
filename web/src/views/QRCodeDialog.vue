<template>
  <el-dialog
    title="签到二维码"
    :visible.sync="visible"
    width="450px"
    @opened="generateQRCode"
    @closed="clearQRCode"
  >
    <div class="qrcode-container">
      <div class="qrcode-wrapper">
        <div ref="qrcode" class="qrcode-canvas"></div>
        <div v-if="loading" class="qrcode-loading">
          <i class="el-icon-loading"></i>
          <p>生成中...</p>
        </div>
      </div>
      
      <div class="qrcode-info">
        <h4>{{ meetingTitle }}</h4>
        <p class="meeting-room">{{ roomName }}</p>
        <p class="meeting-time">
          {{ startTime | formatDateTime }} - {{ endTime | formatTime }}
        </p>
      </div>
      
      <div class="qrcode-tips">
        <el-alert
          title="签到说明"
          type="info"
          :closable="false"
          show-icon
        >
          <ul>
            <li>请在会议开始前30分钟到会议结束后进行签到</li>
            <li>可使用手机扫描二维码或输入签到码</li>
            <li>每人只能签到一次</li>
          </ul>
        </el-alert>
      </div>
      
      <div class="qrcode-actions">
        <el-button @click="refreshQRCode" :loading="loading">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
        <el-button type="primary" @click="downloadQRCode">
          <i class="el-icon-download"></i> 下载
        </el-button>
        <el-button @click="shareQRCode">
          <i class="el-icon-share"></i> 分享
        </el-button>
      </div>
    </div>
    
    <div slot="footer">
      <el-button @click="visible = false">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import QRCode from 'qrcode'

export default {
  name: 'QRCodeDialog',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    bookingId: {
      type: [Number, String],
      required: true
    },
    meetingTitle: {
      type: String,
      default: ''
    },
    roomName: {
      type: String,
      default: ''
    },
    startTime: {
      type: [String, Date],
      default: null
    },
    endTime: {
      type: [String, Date],
      default: null
    }
  },
  data() {
    return {
      loading: false,
      qrCodeUrl: '',
      qrCodeDataUrl: ''
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  methods: {
    async generateQRCode() {
      if (!this.bookingId) return
      
      this.loading = true
      try {
        // 获取签到URL
        const response = await this.$api.bookings.getQRCode(this.bookingId)
        this.qrCodeUrl = response.data
        
        // 生成二维码
        await this.renderQRCode()
        
      } catch (error) {
        this.$message.error('生成二维码失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async renderQRCode() {
      try {
        // 清空之前的内容
        this.$refs.qrcode.innerHTML = ''
        
        // 生成二维码Canvas
        const canvas = document.createElement('canvas')
        await QRCode.toCanvas(canvas, this.qrCodeUrl, {
          width: 200,
          height: 200,
          margin: 2,
          color: {
            dark: '#000000',
            light: '#FFFFFF'
          }
        })
        
        this.$refs.qrcode.appendChild(canvas)
        
        // 保存DataURL用于下载
        this.qrCodeDataUrl = canvas.toDataURL('image/png')
        
      } catch (error) {
        console.error('渲染二维码失败:', error)
        this.$message.error('渲染二维码失败')
      }
    },
    
    async refreshQRCode() {
      await this.generateQRCode()
      this.$message.success('二维码已刷新')
    },
    
    downloadQRCode() {
      if (!this.qrCodeDataUrl) {
        this.$message.warning('二维码未生成完成')
        return
      }
      
      try {
        const link = document.createElement('a')
        link.download = `meeting-qrcode-${this.bookingId}.png`
        link.href = this.qrCodeDataUrl
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        this.$message.success('二维码下载成功')
      } catch (error) {
        this.$message.error('下载失败')
      }
    },
    
    shareQRCode() {
      if (navigator.share) {
        // 使用Web Share API
        navigator.share({
          title: `会议签到 - ${this.meetingTitle}`,
          text: `请扫描二维码签到参加会议`,
          url: this.qrCodeUrl
        }).catch(error => {
          console.error('分享失败:', error)
          this.copyToClipboard()
        })
      } else {
        // 降级到复制链接
        this.copyToClipboard()
      }
    },
    
    copyToClipboard() {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(this.qrCodeUrl).then(() => {
          this.$message.success('签到链接已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopyTextToClipboard()
        })
      } else {
        this.fallbackCopyTextToClipboard()
      }
    },
    
    fallbackCopyTextToClipboard() {
      const textArea = document.createElement('textarea')
      textArea.value = this.qrCodeUrl
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      
      try {
        document.execCommand('copy')
        this.$message.success('签到链接已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败，请手动复制')
      }
      
      document.body.removeChild(textArea)
    },
    
    clearQRCode() {
      if (this.$refs.qrcode) {
        this.$refs.qrcode.innerHTML = ''
      }
      this.qrCodeUrl = ''
      this.qrCodeDataUrl = ''
    }
  }
}
</script>

<style scoped>
.qrcode-container {
  text-align: center;
}

.qrcode-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #e4e7ed;
}

.qrcode-canvas {
  display: block;
}

.qrcode-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #909399;
}

.qrcode-loading i {
  font-size: 24px;
  margin-bottom: 10px;
}

.qrcode-loading p {
  margin: 0;
  font-size: 14px;
}

.qrcode-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.qrcode-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.meeting-room {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

.meeting-time {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 13px;
}

.qrcode-tips {
  margin-bottom: 20px;
  text-align: left;
}

.qrcode-tips ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.qrcode-tips li {
  margin-bottom: 5px;
  font-size: 13px;
  line-height: 1.5;
}

.qrcode-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.qrcode-actions .el-button {
  min-width: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .qrcode-actions {
    flex-direction: column;
  }
  
  .qrcode-actions .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>