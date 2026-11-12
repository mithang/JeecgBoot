# JeecgBoot ShardingSphere Hướng dẫn sử dụng cấu hình

## Cấu hình ShardingSphere trong dự án

Dự án này sử dụng ShardingSphere để triển khai chức năng chia cơ sở dữ liệu/chia bảng, chủ yếu liên quan đến các tệp cấu hình và thành phần sau:

## 1. Giải thích tệp cấu hình

### sharding.yaml - Cấu hình chia bảng cơ bản
```yaml
databaseName: sharding-db  # Quan trọng: phải khớp với@DS tên trong annotation phải nhất quán

dataSources:
  ds0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/jeecg-boot?useSSL=false&useUnicode=true&characterEncoding=utf-8
    username: root
    password: root

rules:
  - !SHARDING
    tables:
      sys_log:  # Tên bảng logic của bảng đã chia
        actualDataNodes: ds0.sys_log$->{0..1}  # Bảng thực tế:sys_log0, sys_log1
        tableStrategy:
          standard:
            shardingColumn: log_type  # Trường phân mảnh
            shardingAlgorithmName: table_inline
    
    shardingAlgorithms:
      table_inline:
        type: INLINE
        props:
          algorithm-expression: sys_log$->{log_type % 2}  # Theo log_type Chia bảng theo phép chia lấy dư
```

### sharding-multi.yaml - Chia cơ sở dữ liệu và bảng+Cấu hình tách đọc/ghi
```yaml
databaseName: sharding-db  # và@DS Giữ annotation nhất quán

dataSources:
  ds0:  # Cơ sở dữ liệu chính
    jdbcUrl: jdbc:mysql://localhost:3306/jeecg-boot?...
  ds1:  # Cơ sở dữ liệu phụ
    jdbcUrl: jdbc:mysql://localhost:3306/jeecg-boot2?...

rules:
  - !SHARDING
    tables:
      sys_log:
        actualDataNodes: ds$->{0..1}.sys_log$->{0..1}  # 2 2 cơ sở dữ liệu 2 bảng
        databaseStrategy:  # Chiến lược chia cơ sở dữ liệu
          standard:
            shardingColumn: operate_type
            shardingAlgorithmName: database-inline
        tableStrategy:  # Chiến lược chia bảng
          standard:
            shardingColumn: log_type
            shardingAlgorithmName: table-classbased

  - !READWRITE_SPLITTING  # Tách đọc/ghi
    dataSources:
      prds:
        writeDataSourceName: ds0  # Cơ sở dữ liệu ghi
        readDataSourceNames: [ds1]  # Cơ sở dữ liệu đọc
```

## 2. Spring Boot Cấu hình

### application-dev.yml Cấu hình nguồn dữ liệu trong

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        # Nguồn dữ liệu thông thường
        master:
          url: jdbc:mysql://localhost:3306/jeecg-boot
          username: root
          password: root
        
        # ShardingSphere Nguồn dữ liệu phân mảnh
        sharding-db:  # Tên nguồn dữ liệu, tương ứng với@DS("sharding-db")
          driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
          # Cách dùng tệp cấu hình cục bộ
          url: jdbc:shardingsphere:classpath:sharding.yaml
          # Hoặc cách cấu hình Nacos
          url: jdbc:shardingsphere:nacos:sharding.yaml?serverAddr=${spring.cloud.nacos.config.server-addr}&namespace=${spring.cloud.nacos.config.namespace}&group=${spring.cloud.nacos.config.group}
```

**Điểm mấu chốt:**
- `sharding-db` là định danh tên của nguồn dữ liệu
- Tên này phải khớp với trên lớp Service`@DS("sharding-db")`Giữ annotation nhất quán

## 3. Service Sử dụng ở tầng

### ShardingSysLogServiceImpl Cấu hình lớp

```java
@Service
@DS("sharding-db")  // Chỉ định sử dụng sharding-db Nguồn dữ liệu
public class ShardingSysLogServiceImpl extends ServiceImpl<ShardingSysLogMapper, ShardingSysLog> 
    implements IShardingSysLogService {
}
```

**Giải thích quan hệ cấu hình:**
1. `@DS("sharding-db")` Annotation cho biết MyBatis-Plus Sử dụng nguồn dữ liệu có tên`sharding-db`của nguồn dữ liệu
2. `sharding-db`Tương ứng với application-dev.yml Tên nguồn dữ liệu được cấu hình trong
3. Nguồn dữ liệu này sử dụng driver ShardingSphere, sẽ dựa theo sharding.yaml quy tắc trong đó để phân mảnh

## 4. Các bước sử dụng

### Bước 1: Chuẩn bị bảng cơ sở dữ liệu
```sql
-- Trong jeecg-boot Tạo bảng đã chia trong cơ sở dữ liệu
CREATE TABLE sys_log0 LIKE sys_log;
CREATE TABLE sys_log1 LIKE sys_log;
```

### Bước 2: Cấu hình application-dev.yml
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        sharding-db:
          driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
          url: jdbc:shardingsphere:classpath:sharding.yaml
```

### Bước 3: Cấu hình sharding.yaml
- Đặt tệp cấu hình vào`src/main/resources/`trong thư mục
- Đảm bảo`databaseName: sharding-db`khớp với tên nguồn dữ liệu

### Bước 4: Thêm annotation trên Service
```java
@DS("sharding-db")  // Sử dụng nguồn dữ liệu phân mảnh
public class ShardingSysLogServiceImpl {
    // Mã nghiệp vụ
}
```

### Bước 5: Sử dụng bình thường MyBatis-Plus
```java
// Khi chèn dữ liệu sẽ tự động dựa theo log_type trường để chia bảng
shardingSysLogService.save(sysLog);

// Khi truy vấn cũng sẽ định tuyến đến đúng bảng theo quy tắc phân mảnh
shardingSysLogService.list();
```

## 5. Xác minh cấu hình

Sau khi khởi động dự án, xem nhật ký, nếu thấy đầu ra tương tự thì cấu hình đã thành công:
```
Logic SQL: INSERT INTO sys_log (log_type, content) VALUES (?, ?)
Actual SQL: ds0 ::: INSERT INTO sys_log0 (log_type, content) VALUES (?, ?)
```

## 6. Lưu ý

1. **Tính nhất quán của tên**: Đảm bảo ba tên sau hoàn toàn nhất quán
   - application-dev.yml Tên nguồn dữ liệu trong:`sharding-db`
   - sharding.yaml databaseName trong:`sharding-db`
   - Service Annotation của lớp:`@DS("sharding-db")`

2. **Cấu trúc bảng nhất quán**: Cấu trúc của mọi bảng phân mảnh phải hoàn toàn nhất quán

3. **Lựa chọn khóa phân mảnh**: Chọn trường có phân bố đều làm khóa phân mảnh, tránh lệch dữ liệu

4. **Hỗ trợ giao dịch**: Giao dịch đơn bảng bình thường, giao dịch liên bảng cần lưu ý

Sau khi cấu hình như vậy, dữ liệu thao tác qua ShardingSysLogServiceImpl sẽ tự động phân bố vào các bảng khác nhau theo quy tắc phân mảnh.
