- Khởi tạo script (mysql)

  db\tables_xxl_job.sql
  
- Sửa kết nối cơ sở dữ liệu

  jeecg-cloud-xxljob\src\main\resources\application.yml
  
- Khởi động dự án

  jeecg-cloud-xxljob\src\main\java\com\xxl\job\admin\XxlJobAdminApplication.java
  
 - Truy cập dự án
  http://127.0.0.1:9080/xxl-job-admin/toLogin
  admin/123456
  
 - Cài đặt bằng Docker
 
   https://my.oschina.net/jeecg/blog/4729020
   
   
   
 Giải thích khái niệm
 1. Tạo executor thủ công, AppName tương ứng với tên service, ví dụ: jeecg-demo
 2. Tạo nhiệm vụ định kỳ thủ công, chọn executor (service), JobHandler tương ứng với giá trị của XxlJob
