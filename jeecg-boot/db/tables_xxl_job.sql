CREATE database if NOT EXISTS `xxl_job` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `xxl_job`;

/*
 Navicat Premium Data Transfer

 Source Server         : mysql5.7
 Source Server Type    : MySQL
 Source Server Version : 50738 (5.7.38)
 Source Host           : 127.0.0.1:3306
 Source Schema         : xxl_job

 Target Server Type    : MySQL
 Target Server Version : 50738 (5.7.38)
 File Encoding         : 65001

 Date: 09/07/2026 13:21:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for xxl_job_group
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_group`;
CREATE TABLE `xxl_job_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'AppName của executor',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Tên executor',
  `address_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Loại địa chỉ executor: 0=Đăng ký tự động, 1=Nhập thủ công',
  `address_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Danh sách địa chỉ executor, nhiều địa chỉ phân tách bằng dấu phẩy',
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_group
-- ----------------------------
INSERT INTO `xxl_job_group` VALUES (1, 'xxl-job-executor-sample', 'Executor chung Sample', 0, NULL, '2026-07-09 11:45:30');
INSERT INTO `xxl_job_group` VALUES (2, 'xxl-job-executor-sample-ai', 'Executor AI Sample', 0, NULL, '2026-07-09 11:45:30');
INSERT INTO `xxl_job_group` VALUES (3, 'jeecg-demo', 'Module Demo kiểm thử', 0, 'http://198.18.0.1:10000/', '2026-07-09 11:45:30');
INSERT INTO `xxl_job_group` VALUES (4, 'jeecg-system', 'Module System hệ thống', 0, 'http://198.18.0.1:10001/', '2026-07-09 11:45:30');

-- ----------------------------
-- Table structure for xxl_job_info
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_info`;
CREATE TABLE `xxl_job_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_group` int(11) NOT NULL COMMENT 'ID khóa chính executor',
  `job_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `add_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `author` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Tác giả',
  `alarm_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Email cảnh báo',
  `schedule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NONE' COMMENT 'Loại lập lịch',
  `schedule_conf` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Cấu hình lập lịch, ý nghĩa giá trị phụ thuộc vào loại lập lịch',
  `misfire_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DO_NOTHING' COMMENT 'Chiến lược quá hạn lập lịch',
  `executor_route_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Chiến lược định tuyến executor',
  `executor_handler` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Handler nhiệm vụ',
  `executor_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Tham số nhiệm vụ',
  `executor_block_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Chiến lược xử lý chặn',
  `executor_timeout` int(11) NOT NULL DEFAULT 0 COMMENT 'Thời gian timeout thực thi nhiệm vụ, đơn vị giây',
  `executor_fail_retry_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Số lần thử lại khi thất bại',
  `glue_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Loại GLUE',
  `glue_source` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Mã nguồn GLUE',
  `glue_remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Ghi chú GLUE',
  `glue_updatetime` datetime NULL DEFAULT NULL COMMENT 'Thời gian cập nhật GLUE',
  `child_jobid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ID nhiệm vụ con, phân tách bằng dấu phẩy',
  `trigger_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Trạng thái lập lịch: 0-Dừng, 1-Chạy',
  `trigger_last_time` bigint(13) NOT NULL DEFAULT 0 COMMENT 'Thời gian lập lịch lần trước',
  `trigger_next_time` bigint(13) NOT NULL DEFAULT 0 COMMENT 'Thời gian lập lịch lần tới',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_info
-- ----------------------------
INSERT INTO `xxl_job_info` VALUES (1, 1, 'Nhiệm vụ mẫu 01', '2026-07-06 19:30:57', '2026-07-06 19:56:12', 'XXL', '', 'CRON', '0 0 0 * * ? *', 'DO_NOTHING', 'FIRST', 'demoJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'Khởi tạo mã GLUE', '2026-07-06 19:30:57', '', 1, 1783440000000, 1783612800000);
INSERT INTO `xxl_job_info` VALUES (2, 2, 'Nhiệm vụ mẫu Ollama', '2026-07-06 19:30:57', '2026-07-06 19:30:57', 'XXL', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'ollamaJobHandler', '{\r\n    \"input\": \"Java triển khai duyệt cây nhị phân theo từng tầng\",\r\n    \"prompt\": \"Bạn là một kỹ sư R&D, giỏi giải quyết các vấn đề kỹ thuật.\",\r\n    \"model\": \"qwen3.5:0.8b\"\r\n}', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'Khởi tạo mã GLUE', '2026-07-06 19:30:57', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (3, 2, 'Nhiệm vụ mẫu Dify', '2026-07-06 19:30:57', '2026-07-06 19:30:57', 'XXL', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'difyWorkflowJobHandler', '{\r\n    \"inputs\":{\r\n        \"input\":\"Truy vấn ba học sinh đứng đầu từng môn của lớp\"\r\n    },\r\n    \"user\": \"xxl-job\",\r\n    \"baseUrl\": \"http://localhost/v1\",\r\n    \"apiKey\": \"app-OUVgNUOQRIMokfmuJvBJoUTN\"\r\n}', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'Khởi tạo mã GLUE', '2026-07-06 19:30:57', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (4, 2, 'Nhiệm vụ mẫu OpenClaw', '2026-07-06 19:30:57', '2026-07-06 19:30:57', 'XXL', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'openClawJobHandler', '{\r\n    \"input\": \"Xem thời tiết Thượng Hải hôm nay, đưa ra gợi ý du lịch\",\r\n    \"prompt\": \"Bạn là một trợ lý du lịch, giỏi lập kế hoạch du lịch\"\r\n}', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'Khởi tạo mã GLUE', '2026-07-06 19:30:57', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (5, 4, 'Kiểm thử jeecg xxljob', '2024-08-21 22:41:10', '2026-07-07 20:50:56', 'JEECG', '', 'CRON', '* * * * * ?', 'DO_NOTHING', 'FIRST', 'demoJob', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'Khởi tạo mã GLUE', '2024-08-21 22:41:10', '', 0, 0, 0);

-- ----------------------------
-- Table structure for xxl_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_lock`;
CREATE TABLE `xxl_job_lock`  (
  `lock_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Tên khóa',
  PRIMARY KEY (`lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_lock
-- ----------------------------
INSERT INTO `xxl_job_lock` VALUES ('schedule_lock');

-- ----------------------------
-- Table structure for xxl_job_log
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_log`;
CREATE TABLE `xxl_job_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_group` int(11) NOT NULL COMMENT 'ID khóa chính executor',
  `job_id` int(11) NOT NULL COMMENT 'Nhiệm vụ, ID khóa chính',
  `executor_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Địa chỉ executor, địa chỉ thực thi lần này',
  `executor_handler` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Handler nhiệm vụ',
  `executor_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Tham số nhiệm vụ',
  `executor_sharding_param` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Tham số phân mảnh nhiệm vụ, định dạng ví dụ 1/2',
  `executor_fail_retry_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Số lần thử lại khi thất bại',
  `trigger_time` datetime NULL DEFAULT NULL COMMENT 'Lập lịch - Thời gian',
  `trigger_code` int(11) NOT NULL COMMENT 'Lập lịch - Kết quả',
  `trigger_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Lập lịch - Nhật ký',
  `handle_time` datetime NULL DEFAULT NULL COMMENT 'Thực thi - Thời gian',
  `handle_code` int(11) NOT NULL COMMENT 'Thực thi - Trạng thái',
  `handle_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Thực thi - Nhật ký',
  `alarm_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Trạng thái cảnh báo: 0-Mặc định, 1-Không cần cảnh báo, 2-Cảnh báo thành công, 3-Cảnh báo thất bại',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `I_trigger_time`(`trigger_time`) USING BTREE,
  INDEX `I_handle_code`(`handle_code`) USING BTREE,
  INDEX `I_jobgroup`(`job_group`) USING BTREE,
  INDEX `I_jobid`(`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18340 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for xxl_job_log_report
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_log_report`;
CREATE TABLE `xxl_job_log_report`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trigger_day` datetime NULL DEFAULT NULL COMMENT 'Lập lịch - Thời gian',
  `running_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Đang chạy - Số lượng nhật ký',
  `suc_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Thực thi thành công - Số lượng nhật ký',
  `fail_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Thực thi thất bại - Số lượng nhật ký',
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_trigger_day`(`trigger_day`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 632 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_log_report
-- ----------------------------

-- ----------------------------
-- Table structure for xxl_job_logglue
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_logglue`;
CREATE TABLE `xxl_job_logglue`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) NOT NULL COMMENT 'Nhiệm vụ, ID khóa chính',
  `glue_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Loại GLUE',
  `glue_source` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Mã nguồn GLUE',
  `glue_remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Ghi chú GLUE',
  `add_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_logglue
-- ----------------------------

-- ----------------------------
-- Table structure for xxl_job_registry
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_registry`;
CREATE TABLE `xxl_job_registry`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `registry_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `registry_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `registry_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_g_k_v`(`registry_group`, `registry_key`, `registry_value`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1036 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_registry
-- ----------------------------
INSERT INTO `xxl_job_registry` VALUES (995, 'EXECUTOR', 'jeecg-demo', 'http://198.18.0.1:10000/', '2026-07-09 11:45:30');
INSERT INTO `xxl_job_registry` VALUES (1029, 'EXECUTOR', 'jeecg-system', 'http://198.18.0.1:10001/', '2026-07-09 11:45:30');

-- ----------------------------
-- Table structure for xxl_job_user
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_user`;
CREATE TABLE `xxl_job_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Tài khoản',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Thông tin mật khẩu đã mã hóa',
  `token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Token đăng nhập',
  `role` tinyint(4) NOT NULL COMMENT 'Vai trò: 0-Người dùng thường, 1-Quản trị viên',
  `permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Quyền hạn: Danh sách ID executor, phân tách bằng dấu phẩy',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_user
-- ----------------------------
INSERT INTO `xxl_job_user` VALUES (1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '118d9c81070e4324b65b0993c6d0b843', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;