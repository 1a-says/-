-- ====================================
-- 图书馆管理系统数据库初始化脚本
-- 数据库: library_management_system
-- 版本: v1.0
-- 创建时间: 2026-01-02
-- ====================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS library_management_system;
CREATE DATABASE library_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_management_system;

-- 2. 创建图书表
DROP TABLE IF EXISTS `tb_book`;
CREATE TABLE `tb_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `collection_number` varchar(50) NOT NULL COMMENT '馆藏号（TS+时间戳+随机数）',
  `isbn` varchar(20) NOT NULL COMMENT 'ISBN号',
  `title` varchar(200) NOT NULL COMMENT '书名',
  `author` varchar(100) NOT NULL COMMENT '作者',
  `publisher` varchar(100) NOT NULL COMMENT '出版社',
  `location` varchar(100) NOT NULL COMMENT '馆藏位置（如：A区-3架-2层）',
  `status` varchar(20) NOT NULL DEFAULT '可借阅' COMMENT '图书状态（可借阅/已借出/遗失/损坏/维护中）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_number` (`collection_number`),
  KEY `idx_isbn` (`isbn`),
  KEY `idx_title` (`title`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书信息表';

-- 3. 创建图书状态历史表
DROP TABLE IF EXISTS `tb_book_status_history`;
CREATE TABLE `tb_book_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `collection_number` varchar(50) NOT NULL COMMENT '馆藏号',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `operator` varchar(50) NOT NULL COMMENT '操作员账号',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_collection_number` (`collection_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书状态历史表';

-- 4. 创建用户表
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_number` varchar(50) NOT NULL COMMENT '学号/工号',
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `identity` varchar(20) NOT NULL COMMENT '身份（教师/学生）',
  `card_number` varchar(50) NOT NULL COMMENT '校园卡号',
  `password` varchar(255) NOT NULL COMMENT '密码（SHA256加密）',
  `initial_password` varchar(50) NOT NULL COMMENT '初始密码（明文，用于重置）',
  `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色（admin/user）',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '账号状态（正常/锁定/停用）',
  `lock_time` datetime DEFAULT NULL COMMENT '锁定时间',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_number` (`account_number`),
  UNIQUE KEY `uk_card_number` (`card_number`),
  KEY `idx_identity` (`identity`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 5. 创建借阅记录表
DROP TABLE IF EXISTS `tb_borrow_record`;
CREATE TABLE `tb_borrow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` varchar(50) NOT NULL COMMENT '借阅记录ID（BR+时间戳）',
  `card_number` varchar(50) NOT NULL COMMENT '校园卡号',
  `user_name` varchar(100) NOT NULL COMMENT '用户姓名',
  `user_identity` varchar(20) NOT NULL COMMENT '用户身份',
  `account_number` varchar(50) NOT NULL COMMENT '学号/工号',
  `collection_number` varchar(50) NOT NULL COMMENT '馆藏号',
  `book_title` varchar(200) NOT NULL COMMENT '图书书名',
  `book_author` varchar(100) NOT NULL COMMENT '图书作者',
  `borrow_date` datetime NOT NULL COMMENT '借阅日期',
  `due_date` datetime NOT NULL COMMENT '应还日期',
  `return_date` datetime DEFAULT NULL COMMENT '归还日期',
  `overdue_days` int NOT NULL DEFAULT 0 COMMENT '超期天数',
  `status` varchar(20) NOT NULL DEFAULT '借阅中' COMMENT '借阅状态（借阅中/已归还）',
  `operator` varchar(50) NOT NULL COMMENT '操作员账号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_id` (`record_id`),
  KEY `idx_card_number` (`card_number`),
  KEY `idx_account_number` (`account_number`),
  KEY `idx_collection_number` (`collection_number`),
  KEY `idx_status` (`status`),
  KEY `idx_borrow_date` (`borrow_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 6. 创建系统配置表
DROP TABLE IF EXISTS `tb_system_config`;
CREATE TABLE `tb_system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(200) NOT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 7. 插入初始数据

-- 插入管理员账号（密码：admin，SHA256加密后的值）
INSERT INTO `tb_user` (`account_number`, `name`, `identity`, `card_number`, `password`, `initial_password`, `role`, `status`) 
VALUES ('admin', '系统管理员', '教师', 'ADMIN001', 
        'c1c224b03cd9bc7b6a86d77f5dace40191766c485cd55dc48caf9ac873335d6f', 
        'admin', 'admin', '正常');

-- 插入系统默认配置
INSERT INTO `tb_system_config` (`config_key`, `config_value`, `description`) VALUES
('teacher_borrow_days', '90', '教师借阅期限（天）'),
('student_borrow_days', '60', '学生借阅期限（天）'),
('max_borrow_count', '5', '单次最大借阅数量（本）');

-- 8. 插入测试数据（可选）

-- 插入测试学生用户
INSERT INTO `tb_user` (`account_number`, `name`, `identity`, `card_number`, `password`, `initial_password`, `role`, `status`) 
VALUES 
('2021001', '张三', '学生', 'C2021001', 
 'e5c0cd39e2e0c0556ab1c5e2e8e0b02bbfeb7e573e03e4f60e5d1a0c8e5d5b5f', 
 '021001', 'user', '正常'),
('2021002', '李四', '学生', 'C2021002', 
 'a5c0cd39e2e0c0556ab1c5e2e8e0b02bbfeb7e573e03e4f60e5d1a0c8e5d5b5f', 
 '021002', 'user', '正常'),
('T001', '王老师', '教师', 'CT001', 
 'b5c0cd39e2e0c0556ab1c5e2e8e0b02bbfeb7e573e03e4f60e5d1a0c8e5d5b5f', 
 'T001', 'user', '正常');

-- 插入测试图书
INSERT INTO `tb_book` (`collection_number`, `isbn`, `title`, `author`, `publisher`, `location`, `status`) VALUES
('TS20260101120000001', '978-7-115-54602-3', '算法导论（原书第3版）', 'Thomas H.Cormen', '机械工业出版社', 'A区-3架-2层', '可借阅'),
('TS20260101130000002', '978-7-111-54742-6', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', 'A区-2架-1层', '可借阅'),
('TS20260101140000003', '978-7-111-54493-7', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 'B区-1架-3层', '可借阅'),
('TS20260102150000004', '978-7-115-52889-0', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', 'A区-5架-2层', '可借阅'),
('TS20260102160000005', '978-7-111-55140-9', '设计模式：可复用面向对象软件的基础', 'Erich Gamma', '机械工业出版社', 'B区-3架-1层', '可借阅');

-- 完成
SELECT '数据库初始化完成！' AS message;
