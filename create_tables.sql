-- 会议室预约签到系统数据库初始化脚本
-- 适用于Oracle数据库

-- 删除已存在的表（如果存在）
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE t_notification CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE t_meeting_checkin CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE t_meeting_booking CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE t_meeting_room CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE t_user CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- 删除序列（如果存在）
BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_user';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_meeting_room';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_meeting_booking';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_meeting_checkin';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_notification';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- 用户表
CREATE TABLE t_user (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100),
    phone VARCHAR2(20),
    role VARCHAR2(20) DEFAULT 'USER', -- USER, ADMIN
    status NUMBER(1) DEFAULT 1, -- 1:启用 0:禁用
    create_time DATE DEFAULT SYSDATE,
    update_time DATE DEFAULT SYSDATE
);

-- 会议室表
CREATE TABLE t_meeting_room (
    id NUMBER PRIMARY KEY,
    room_name VARCHAR2(100) NOT NULL,
    room_location VARCHAR2(200),
    capacity NUMBER NOT NULL,
    equipment VARCHAR2(500), -- 设备信息，JSON格式
    status NUMBER(1) DEFAULT 1, -- 1:可用 0:维护中
    create_time DATE DEFAULT SYSDATE,
    update_time DATE DEFAULT SYSDATE
);

-- 会议预订表
CREATE TABLE t_meeting_booking (
    id NUMBER PRIMARY KEY,
    room_id NUMBER NOT NULL,
    user_id NUMBER NOT NULL,
    meeting_title VARCHAR2(200) NOT NULL,
    meeting_desc VARCHAR2(1000),
    start_time DATE NOT NULL,
    end_time DATE NOT NULL,
    attendee_count NUMBER DEFAULT 1,
    attendees VARCHAR2(2000), -- 参会人员，JSON格式
    booking_status VARCHAR2(20) DEFAULT 'BOOKED', -- BOOKED:已预订 CANCELLED:已取消 COMPLETED:已完成
    remind_time NUMBER DEFAULT 15, -- 提前提醒时间（分钟）
    is_reminded NUMBER(1) DEFAULT 0, -- 是否已提醒
    create_time DATE DEFAULT SYSDATE,
    update_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (room_id) REFERENCES t_meeting_room(id),
    FOREIGN KEY (user_id) REFERENCES t_user(id)
);

-- 签到记录表
CREATE TABLE t_meeting_checkin (
    id NUMBER PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    user_id NUMBER NOT NULL,
    checkin_type VARCHAR2(20) NOT NULL, -- QR_CODE:扫码 NFC:NFC MANUAL:手动
    checkin_time DATE DEFAULT SYSDATE,
    checkin_status VARCHAR2(20) DEFAULT 'PRESENT', -- PRESENT:出席 ABSENT:缺席
    FOREIGN KEY (booking_id) REFERENCES t_meeting_booking(id),
    FOREIGN KEY (user_id) REFERENCES t_user(id)
);

-- 系统通知表
CREATE TABLE t_notification (
    id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    booking_id NUMBER,
    title VARCHAR2(200) NOT NULL,
    content VARCHAR2(1000) NOT NULL,
    type VARCHAR2(20) DEFAULT 'SYSTEM', -- SYSTEM:系统通知 MEETING:会议提醒
    is_read NUMBER(1) DEFAULT 0,
    create_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (user_id) REFERENCES t_user(id),
    FOREIGN KEY (booking_id) REFERENCES t_meeting_booking(id)
);

-- 创建序列
CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_meeting_room START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_meeting_booking START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_meeting_checkin START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_notification START WITH 1 INCREMENT BY 1;

-- 创建触发器（自动递增ID）
CREATE OR REPLACE TRIGGER trg_user_id
    BEFORE INSERT ON t_user
    FOR EACH ROW
BEGIN
    SELECT seq_user.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

CREATE OR REPLACE TRIGGER trg_meeting_room_id
    BEFORE INSERT ON t_meeting_room
    FOR EACH ROW
BEGIN
    SELECT seq_meeting_room.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

CREATE OR REPLACE TRIGGER trg_meeting_booking_id
    BEFORE INSERT ON t_meeting_booking
    FOR EACH ROW
BEGIN
    SELECT seq_meeting_booking.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

CREATE OR REPLACE TRIGGER trg_meeting_checkin_id
    BEFORE INSERT ON t_meeting_checkin
    FOR EACH ROW
BEGIN
    SELECT seq_meeting_checkin.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

CREATE OR REPLACE TRIGGER trg_notification_id
    BEFORE INSERT ON t_notification
    FOR EACH ROW
BEGIN
    SELECT seq_notification.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

-- 创建索引
CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_booking_room_time ON t_meeting_booking(room_id, start_time, end_time);
CREATE INDEX idx_booking_user ON t_meeting_booking(user_id);
CREATE INDEX idx_booking_status ON t_meeting_booking(booking_status);
CREATE INDEX idx_checkin_booking ON t_meeting_checkin(booking_id);
CREATE INDEX idx_checkin_user ON t_meeting_checkin(user_id);
CREATE INDEX idx_notification_user ON t_notification(user_id);

-- 插入初始数据

-- 管理员用户 (密码: admin123)
INSERT INTO t_user (username, password, real_name, email, role) 
VALUES ('admin', '$2a$10$YzQG9VNOuNFZKJdQGz4bC.Z0l5GJThJlJthD5R.XK9wQFJ3TFK4yK', '系统管理员', 'admin@company.com', 'ADMIN');

-- 普通用户 (密码: user123)
INSERT INTO t_user (username, password, real_name, email, role) 
VALUES ('user001', '$2a$10$YzQG9VNOuNFZKJdQGz4bC.Z0l5GJThJlJthD5R.XK9wQFJ3TFK4yK', '张三', 'zhangsan@company.com', 'USER');

INSERT INTO t_user (username, password, real_name, email, role) 
VALUES ('user002', '$2a$10$YzQG9VNOuNFZKJdQGz4bC.Z0l5GJThJlJthD5R.XK9wQFJ3TFK4yK', '李四', 'lisi@company.com', 'USER');

-- 会议室数据
INSERT INTO t_meeting_room (room_name, room_location, capacity, equipment) 
VALUES ('大会议室A', '1楼东侧', 20, '{"projector": true, "whiteboard": true, "videoConference": true, "microphone": true}');

INSERT INTO t_meeting_room (room_name, room_location, capacity, equipment) 
VALUES ('小会议室B', '2楼西侧', 8, '{"projector": true, "whiteboard": false, "videoConference": false, "microphone": false}');

INSERT INTO t_meeting_room (room_name, room_location, capacity, equipment) 
VALUES ('培训室C', '3楼北侧', 30, '{"projector": true, "whiteboard": true, "videoConference": true, "microphone": true}');

INSERT INTO t_meeting_room (room_name, room_location, capacity, equipment) 
VALUES ('讨论室D', '2楼东侧', 6, '{"projector": false, "whiteboard": true, "videoConference": false, "microphone": false}');

-- 提交事务
COMMIT;

-- 查看创建结果
SELECT 'Users: ' || COUNT(*) FROM t_user;
SELECT 'Rooms: ' || COUNT(*) FROM t_meeting_room;

PROMPT 数据库初始化完成！
PROMPT 默认管理员账号: admin / admin123
PROMPT 默认用户账号: user001 / user123, user002 / user123