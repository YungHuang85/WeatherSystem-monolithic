# WeatherSystem-monolithic

    基於 Spring Boot 與 Quartz 的天氣排程系統，透過 Open-Meteo API 定期抓取台北市、新北市與桃園市的天氣資訊，並以報表方式推送至 Microsoft Teams。

# 專案特色

    - 使用 Quartz 設定排程，每 30 秒自動抓取天氣資料
    - 整合 Open-Meteo API 擷取天氣資料（氣溫、降雨等）
    - 報表格式化後推送至 Microsoft Teams
    - 採用 Spring Boot 與 AOP 設計，模組化清晰
    - 支援資料儲存至資料庫（MSSQL）

# 專案結構

    WeatherSystem-monolithic/
    ├── client/ # WebClient API 呼叫
    ├── controller/ # REST 控制器（如有）
    ├── dto/ # 資料傳輸物件 (WeatherData)
    ├── job/ # Quartz 排程任務
    ├── service/ # 天氣分析與 Teams 通知邏輯
    ├── util/ # 公用工具類別
    ├── config/ # 設定類（Quartz、WebClient 等）
    ├── resources/
    │ └── application.yml # 設定檔（API 位置、Teams Webhook、排程間隔）

# 技術使用
    Spring Boot 3.4.1
    Spring Web (WebClient)
    Quartz Scheduler
    Lombok
    Microsoft Teams Webhook
    AOP 
