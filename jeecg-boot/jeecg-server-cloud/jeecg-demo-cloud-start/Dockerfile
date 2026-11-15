FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

MAINTAINER jeecgos@163.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# Khắc phục lỗi font tiếng Trung: image cơ sở đã có locale C.utf8, dùng trực tiếp, không cần yum cài glibc-langpack-en (tránh mạng chậm khi build)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

RUN mkdir -p /jeecg-demo-cloud

WORKDIR /jeecg-demo-cloud

EXPOSE 7002

ADD ./target/jeecg-demo-cloud-start-3.9.6.jar ./

# Bỏ sleep 60, container khởi động xong kéo app lên ngay (Nacos sẵn sàng được bảo đảm bởi healthcheck + depends_on: service_healthy của docker-compose)
CMD exec java -Djava.security.egd=file:/dev/./urandom -jar jeecg-demo-cloud-start-3.9.6.jar
