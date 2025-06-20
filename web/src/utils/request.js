import moment from 'moment'

// 时间格式化工具
export const formatTime = {
    // 格式化日期时间
    datetime: (date) => {
        if (!date) return ''
        return moment(date).format('YYYY-MM-DD HH:mm:ss')
    },

    // 格式化日期
    date: (date) => {
        if (!date) return ''
        return moment(date).format('YYYY-MM-DD')
    },

    // 格式化时间
    time: (date) => {
        if (!date) return ''
        return moment(date).format('HH:mm')
    },

    // 相对时间
    fromNow: (date) => {
        if (!date) return ''
        return moment(date).fromNow()
    },

    // 计算时长
    duration: (startTime, endTime) => {
        if (!startTime || !endTime) return ''
        const start = moment(startTime)
        const end = moment(endTime)
        const duration = moment.duration(end.diff(start))

        const hours = Math.floor(duration.asHours())
        const minutes = duration.minutes()

        if (hours > 0) {
            return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`
        } else {
            return `${minutes}分钟`
        }
    }
}

// 验证工具
export const validate = {
    // 邮箱验证
    email: (email) => {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        return re.test(email)
    },

    // 手机号验证
    phone: (phone) => {
        const re = /^1[3-9]\d{9}$/
        return re.test(phone)
    },

    // 密码强度验证
    password: (password) => {
        return password && password.length >= 6
    }
}

// 文件下载
export const downloadFile = (url, filename) => {
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}

// 复制到剪贴板
export const copyToClipboard = (text) => {
    if (navigator.clipboard) {
        return navigator.clipboard.writeText(text)
    } else {
        const textArea = document.createElement('textarea')
        textArea.value = text
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        return Promise.resolve()
    }
}

// 防抖函数
export const debounce = (func, wait) => {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

// 节流函数
export const throttle = (func, limit) => {
    let inThrottle
    return function() {
        const args = arguments
        const context = this
        if (!inThrottle) {
            func.apply(context, args)
            inThrottle = true
            setTimeout(() => inThrottle = false, limit)
        }
    }
}