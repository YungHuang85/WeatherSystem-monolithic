# 使用 JDK 17 作為基底
FROM eclipse-temurin:17-jdk

# 建立應用程式工作目錄
WORKDIR /app

# 複製 jar 檔進容器（後面會在 Step 3 建立這個 jar）
COPY target/Quartz-Project-Weather-Quartz-Project-Weather.jar app.jar

# 開放 8080 port（Spring Boot 預設）
EXPOSE 8080

# 啟動指令
ENTRYPOINT ["java", "-jar", "app.jar"]
