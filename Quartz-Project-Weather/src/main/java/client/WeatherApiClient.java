package client;

import dto.WeatherData;
import java.time.YearMonth;
import java.util.List;

/**
 * 定義「取得某年某月天氣資料」的方法
 */
public interface WeatherApiClient {
    /**
     * 取得指定年月、指定經緯度的每日天氣資料
     *
     * @param ym   年月 (e.g. YearMonth.of(2025, 5))
     * @param lat  緯度
     * @param lon  經度
     * @return     一個 List，每個元素包含該日日期、最低溫、最高溫與降水量
     */
    List<WeatherData> fetchMonthlyData(YearMonth ym, double lat, double lon);
}
