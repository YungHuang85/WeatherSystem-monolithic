package dto;

import java.time.LocalDate;

/**
 * 代表單日天氣資料：日期、最低溫、最高溫、降水量
 */
public record WeatherData(
    LocalDate date,
    double minTemp,
    double maxTemp,
    double precipitation
) {}
