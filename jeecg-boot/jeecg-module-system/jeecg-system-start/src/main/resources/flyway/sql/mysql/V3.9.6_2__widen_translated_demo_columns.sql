-- V3.9.6_2__widen_translated_demo_columns.sql
-- Phase 3 (P3-01, 2026-11-16): widen 12 columns whose translated Vietnamese
-- seed values are longer than the original Chinese-sized varchar columns.
-- Without this, importing the translated seed jeecgboot-mysql-5.7.sql fails
-- with MySQL error 1406 "Data too long" (398 rows). Applied migrations are
-- never edited in place; this is a NEW migration. Safe: widen-only ALTERs.

ALTER TABLE `ins_claim` MODIFY COLUMN `claim_type` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Loại bồi thường（Chi phí y tế/Bồi thường tử vong/Thiệt hại xe cộ/Thương tích do tai nạn/Chi trả bệnh hiểm nghèo）';
ALTER TABLE `ins_claim` MODIFY COLUMN `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Trạng thái bồi thường（Chờ xét duyệt/Đã bồi thường/Đã từ chối bồi thường）';
ALTER TABLE `ins_customer` MODIFY COLUMN `city` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Thành phố';
ALTER TABLE `ins_customer` MODIFY COLUMN `cust_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Hạng khách hàng（Thông thường/Thẻ bạc/Thẻ vàng/Bạch kim）';
ALTER TABLE `ins_customer` MODIFY COLUMN `occupation` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Nghề nghiệp';
ALTER TABLE `ins_policy` MODIFY COLUMN `channel` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Kênh bán hàng（Bán trực tiếp trực tuyến/Đại lý/Bảo hiểm qua ngân hàng/Bán hàng qua điện thoại/Môi giới）';
ALTER TABLE `ins_policy` MODIFY COLUMN `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Trạng thái hợp đồng bảo hiểm（Có hiệu lực/Đã hết hạn/Hủy hợp đồng bảo hiểm）';
ALTER TABLE `jimu_report` MODIFY COLUMN `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Tên';
ALTER TABLE `jimu_report_db` MODIFY COLUMN `db_ch_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Tên tập dữ liệu';
ALTER TABLE `rep_demo_dxtj` MODIFY COLUMN `hukou` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Nơi đăng ký hộ khẩu';
ALTER TABLE `sys_tenant_pack` MODIFY COLUMN `pack_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên gói sản phẩm';
ALTER TABLE `test_note` MODIFY COLUMN `contents` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Lý do nghỉ phép';
ALTER TABLE `airag_prompts` MODIFY COLUMN `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Mô tả chức năng từ nhắc';
ALTER TABLE `ins_claim` MODIFY COLUMN `province` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tỉnh xảy ra rủi ro';
ALTER TABLE `ins_customer` MODIFY COLUMN `province` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tỉnh';
ALTER TABLE `ins_policy` MODIFY COLUMN `province` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tỉnh phát hành đơn';
