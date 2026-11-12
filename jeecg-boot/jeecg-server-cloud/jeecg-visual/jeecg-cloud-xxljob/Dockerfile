FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

MAINTAINER jeecgos@163.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# Khắc phục lỗi hiển thị ký tự tiếng Trung: image cơ sở đã tích hợp sẵn locale C.utf8, dùng trực tiếp, không cần cài glibc-langpack-en bằng yum (tránh mạng chậm khi build)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

RUN mkdir -p /jeecg-cloud-xxljob

WORKDIR /jeecg-cloud-xxljob

EXPOSE 9080

ADD ./target/jeecg-cloud-xxljob-3.9.6.jar ./

CMD exec java -Djava.security.egd=file:/dev/./urandom -jar jeecg-cloud-xxljob-3.9.6.jar

