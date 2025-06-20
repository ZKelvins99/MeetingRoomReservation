

# 会议室预约签到系统

根据公司技术比武题目，本项目（包括README）由claude4.0生成，并手动修改一些内容，仍有部分未完成。

一个基于SpringBoot + Vue2 + Element UI的企业级会议室预约签到系统，支持会议室预订、多种签到方式、实时统计等功能。


## 功能特性

### 🏢 会议室管理
- 会议室信息维护（名称、位置、容量、设备配置）
- 会议室状态管理（可用/维护中）
- 会议室使用统计和分析

### 📅 预订管理
- 在线预订会议室
- 时间冲突检测
- 周期性预订支持
- 预订提醒功能
- 预订历史查询

### ✅ 签到管理
- 二维码扫码签到
- NFC签到支持
- 手动工号签到
- 实时签到统计
- 签到状态管理

### 📊 统计报表
- 会议室使用率统计
- 用户活跃度分析
- 时段使用分布
- 数据导出功能

### 🔐 权限管理
- 用户角色管理（普通用户/管理员）
- 功能权限控制
- JWT身份验证

### 📱 多端适配
- 响应式设计
- PC端/移动端兼容
- 渐进式Web应用

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **数据库**: Oracle 11g/12c
- **ORM**: MyBatis
- **安全**: Spring Security + JWT
- **工具**: Lombok, Apache Commons

### 前端
- **框架**: Vue 2.6.14
- **UI组件**: Element UI 2.15.14
- **状态管理**: Vuex
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **图表**: ECharts
- **二维码**: qrcode.js

## 快速开始

### 环境要求

- **JDK**: 1.8+
- **Maven**: 3.6+
- **Node.js**: 14+
- **Oracle**: 11g/12c
- **浏览器**: Chrome 70+, Firefox 65+, Safari 12+

### 数据库初始化

1. 连接到Oracle数据库：
```bash
sqlplus username/password@host:port/service_name
```

2. 执行建表脚本：
```sql
@database/create_tables.sql
```

### 后端启动

1. 克隆项目：
```bash
git clone <repository-url>
cd meeting-room-system
```

2. 修改配置文件：
```bash
# 编辑 src/main/resources/application.properties
# 修改数据库连接信息
spring.datasource.url=jdbc:oracle:thin:@your-host:1521:your-service
spring.datasource.username=your-username
spring.datasource.password=your-password
```

3. 编译启动：
```bash
mvn clean install
mvn spring-boot:run
```

或在IDE中直接运行 `MeetingRoomSystemApplication.java`

### 前端启动

1. 进入前端目录：
```bash
cd meeting-room-frontend
```

2. 安装依赖：
```bash
npm install
# 或使用 yarn
yarn install
```

3. 启动开发服务器：
```bash
npm run serve
# 或使用 yarn
yarn serve
```

4. 浏览器访问：
```
http://localhost:8081
```

### 默认账号

- **管理员**: admin / admin123
- **普通用户**: user001 / user123

## 项目结构

### 后端结构
```
src/main/java/com/company/meeting/
├── MeetingRoomSystemApplication.java    # 主启动类
├── config/                              # 配置类
├── controller/                          # 控制器
├── service/                             # 服务层
├── mapper/                              # 数据访问层
├── entity/                              # 实体类
├── dto/                                 # 数据传输对象
├── common/                              # 公共类
├── util/                                # 工具类
├── security/                            # 安全相关
└── task/                                # 定时任务

src/main/resources/
├── application.properties               # 配置文件
├── mapper/                             # MyBatis映射文件
└── static/                             # 静态资源
```

### 前端结构
```
src/
├── main.js                             # 入口文件
├── App.vue                             # 根组件
├── router/                             # 路由配置
├── store/                              # 状态管理
├── api/                                # API接口
├── components/                         # 公共组件
├── views/                              # 页面组件
├── assets/                             # 静态资源
└── utils/                              # 工具函数
```

## API文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取用户信息

### 会议室接口
- `GET /api/rooms/list` - 获取会议室列表
- `GET /api/rooms/available` - 获取可用会议室
- `POST /api/rooms/add` - 添加会议室
- `PUT /api/rooms/update` - 更新会议室
- `DELETE /api/rooms/{id}` - 删除会议室

### 预订接口
- `POST /api/bookings/book` - 预订会议室
- `GET /api/bookings/my` - 获取我的预订
- `GET /api/bookings/all` - 获取所有预订
- `PUT /api/bookings/{id}/cancel` - 取消预订
- `GET /api/bookings/{id}/qrcode` - 获取签到二维码

### 签到接口
- `POST /api/checkin/scan` - 扫码签到
- `POST /api/checkin/manual` - 手动签到
- `GET /api/checkin/list` - 获取签到列表
- `GET /api/checkin/statistics/{bookingId}` - 获取签到统计

## 部署指南

### 生产环境部署

1. **后端部署**：
```bash
# 打包
mvn clean package -Dmaven.test.skip=true

# 运行
java -jar target/meeting-room-system-1.0.0.jar --spring.profiles.active=prod
```

2. **前端部署**：
```bash
# 构建
npm run build

# 部署到Web服务器（如Nginx）
cp -r dist/* /var/www/html/
```

3. **Nginx配置示例**：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 配置说明

### 数据库配置
```properties
# Oracle数据库连接配置
spring.datasource.url=jdbc:oracle:thin:@host:port:service
spring.datasource.username=username
spring.datasource.password=password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

### JWT配置
```properties
# JWT密钥和过期时间
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### 邮件配置
```properties
# 邮件服务器配置（可选）
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=your-email@qq.com
spring.mail.password=your-auth-code
```

## 开发指南

### 添加新功能
1. 创建实体类和DTO
2. 编写Mapper接口和XML
3. 实现Service业务逻辑
4. 添加Controller接口
5. 前端添加API调用
6. 实现页面组件


### v1.0.0 (2025-06-19)
- ✅ 基础会议室管理功能
- ✅ 会议预订和取消功能
- ✅ 多种签到方式支持
- ✅ 统计报表功能
- ✅ 用户权限管理
- ✅ 响应式界面设计

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 致谢

感谢以下开源项目：
- Spring Boot
- Vue.js
- Element UI
- MyBatis
- ECharts
- QRCode.js

---

