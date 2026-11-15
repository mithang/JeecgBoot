FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:21-anolis

MAINTAINER jeecgos@163.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# Khắc phục lỗi font tiếng Trung: image cơ sở đã có locale C.utf8, dùng trực tiếp, không cần yum cài glibc-langpack-en (tránh mạng chậm khi build)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

RUN mkdir -p /jeecg-cloud-nacos

WORKDIR /jeecg-cloud-nacos

EXPOSE 8848 18080

ADD ./target/jeecg-cloud-nacos-3.9.6.jar ./

# Bỏ sleep 30, container khởi động xong kéo app lên ngay (MySQL sẵn sàng được bảo đảm bởi healthcheck + depends_on: service_healthy của docker-compose)
CMD exec java -Djava.security.egd=file:/dev/./urandom -jar jeecg-cloud-nacos-3.9.6.jar