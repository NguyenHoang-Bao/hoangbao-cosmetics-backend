# ==========================================
# Dockerfile cho Hoang Bao Cosmetics Backend
# Tối ưu hóa bộ nhớ cho VPS 1GB RAM & Timezone GMT+7
# ==========================================

# Stage 1: Runtime Image với Eclipse Temurin Java 17 JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cài đặt múi giờ Việt Nam (Asia/Ho_Chi_Minh) trong Linux Container
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# Sao chép file JAR thành phẩm
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# Chạy JVM với cấu hình giới hạn RAM 70%, G1GC và múi giờ GMT+7
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-XX:+UseG1GC", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]
