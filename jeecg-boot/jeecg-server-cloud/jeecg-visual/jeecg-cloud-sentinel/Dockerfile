FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

MAINTAINER jeecgos@163.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# Khắc phục lỗi font tiếng Trung: image cơ sở đã có locale C.utf8, dùng trực tiếp, không cần yum cài glibc-langpack-en (tránh mạng chậm khi build)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

RUN mkdir -p /jeecg-cloud-sentinel

WORKDIR /jeecg-cloud-sentinel

EXPOSE 8848

ADD ./target/jeecg-cloud-sentinel-3.9.6.jar ./

# Bỏ sleep 5, khởi động trực tiếp (các dependency sẵn sàng được bảo đảm bởi healthcheck + depends_on của docker-compose)
CMD exec java -Djava.security.egd=file:/dev/./urandom -jar jeecg-cloud-sentinel-3.9.6.jar
