# Quy tắc đặt tên file SQL
`V[ngày tháng năm]_[số thứ tự]__[tên viết tắt module]_[loại thao tác]_[mô tả nghiệp vụ].sql`

Ví dụ:
```
V20240104_1__easyoa_add_field_attendance.sql
R__202402_drag_update_template.sql
```

### I. Hướng dẫn quy tắc đặt tên SQL
- 1. Chỉ cần thực thi một lần thì bắt đầu bằng chữ hoa “V”
- 2. Cần thực thi nhiều lần thì bắt đầu bằng chữ hoa “R”, đặt tên dạng R__clean.sql, script R chỉ cần thay đổi là sẽ được thực thi
- 3. Script bắt đầu bằng V có độ ưu tiên cao hơn script bắt đầu bằng R.
- 4. Tham khảo blog: https://blog.csdn.net/Jiao1225/article/details/129590660

### II. Lưu trữ SQL tăng dần
- 1. Nén và lưu trữ tất cả các file SQL trong thư mục vào thư mục `backup`
```
  Thư mục: `jeecg-system-start\src\main\resources\flyway\sql\mysql`
```
- 2. Thực thi SQL
```
-- Xóa nhật ký thực thi tăng dần lịch sử
delete from flyway_schema_history where installed_rank > 1;
```
- 3. Như vậy là gọn gàng, có thể bắt đầu một khởi đầu mới cho dự án
