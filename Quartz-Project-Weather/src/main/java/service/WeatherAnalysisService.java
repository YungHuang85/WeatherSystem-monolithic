package service;

import client.WeatherApiClient;
import dto.WeatherData;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service // 標示為 Spring 的服務元件，供 DI 容器管理
public class WeatherAnalysisService {

    // 天氣 API 的資料抓取服務
    private final WeatherApiClient apiClient;

    // Microsoft Teams 通知發送服務
    private final TeamsNotifyService teamsNotifyService;

    // 台北的經緯度座標（固定值）
    private static final double LAT = 25.0330;
    private static final double LON = 121.5654;

    // 日期格式：例如「6月2日」
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M月d日");

    // 建構子注入(DI) API 與 Teams 通知服務
    public WeatherAnalysisService(WeatherApiClient apiClient, TeamsNotifyService teamsNotifyService) {
        this.apiClient = apiClient;
        this.teamsNotifyService = teamsNotifyService;
    }

    // 主邏輯：產出指定月份的天氣報告與統計資料
    public void reportAndStats(YearMonth ym) {
        // 呼叫天氣 API 取得該月份每日的天氣資料
        List<WeatherData> data = apiClient.fetchMonthlyData(ym, LAT, LON);

        // 計算每天的平均氣溫（高溫與低溫的平均）
        Map<LocalDate, Double> dailyAvg = data.stream()
            .collect(Collectors.toMap(
                WeatherData::date,
                d -> (d.maxTemp() + d.minTemp()) / 2.0
            ));

        // 全月份每日平均氣溫的平均值，用來區分「熱」或「冷」
        double avgOfDailyAvg = dailyAvg.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(Double.NaN);

        // 根據最低氣溫排序，取出溫度最低的前三天
        LocalDate[] lowest3Dates = data.stream()
            // 按照每日最低氣溫 (minTemp) 從小到大排序
            .sorted(Comparator.comparingDouble(WeatherData::minTemp))
            // 只取出排序後的前三筆資料（即最低的 3 天）
            .limit(3)
            // 只保留日期欄位（LocalDate）
            .map(WeatherData::date)
            // 再次將這三天做日期排序（避免結果是亂序）
            .sorted()
            // 將結果轉成 LocalDate 陣列
            .toArray(LocalDate[]::new);


        // 根據最高氣溫排序，取出溫度最高的前三天
        LocalDate[] highest3Dates = data.stream()
            // 按照每日最高氣溫 (maxTemp) 從大到小排序
            .sorted(Comparator.comparingDouble(WeatherData::maxTemp).reversed())
            // 只取出排序後的前三筆資料（即最高的 3 天）
            .limit(3)
            // 只保留日期欄位（LocalDate）
            .map(WeatherData::date)
            // 再次將這三天做日期排序（避免結果是亂序）
            .sorted()
            // 將結果轉成 LocalDate 陣列
            .toArray(LocalDate[]::new);

        // 計算最低三天的平均「低溫」
        double avgLow = Arrays.stream(lowest3Dates)
            .mapToDouble(date -> data.stream()
                .filter(d -> d.date().equals(date))
                .findFirst()
                .map(WeatherData::minTemp)
                .orElse(Double.NaN))
            .average()
            .orElse(Double.NaN);

        // 計算最高三天的平均「高溫」
        double avgHigh = Arrays.stream(highest3Dates)
            .mapToDouble(date -> data.stream()
                .filter(d -> d.date().equals(date))
                .findFirst()
                .map(WeatherData::maxTemp)
                .orElse(Double.NaN))
            .average()
            .orElse(Double.NaN);

        // 找出連續 5 天以上下雨的天氣資料段落
        // 利用日期與 index 的差值分組，能捕捉「連續」天的資料
        List<List<WeatherData>> rainyRuns = data.stream()
            .collect(Collectors.groupingBy(d -> d.date().toEpochDay() - data.indexOf(d)))
            .values().stream()
            .filter(group ->
                group.size() >= 5 &&
                group.stream().allMatch(d -> d.precipitation() > 0) // 全部都有降雨
            )
            .toList();

        // 開始建構報表訊息文字
        StringBuilder msg = new StringBuilder();
        msg.append("每日天氣報告（").append(ym.getMonthValue()).append("月）\n\n");
        msg.append("日期 | 晴/雨 | 熱/冷 | 高溫 | 低溫 | 平均\n");
        msg.append("--- | --- | --- | --- | --- | ---\n");

        // 將每天的資料列出：包含是否下雨、是否高於平均等資訊
        for (WeatherData d : data) {
            String date = d.date().format(DATE_FMT);
            String rain = d.precipitation() > 0 ? "雨" : "晴";
            double avg = dailyAvg.get(d.date());
            String hotCold = avg >= avgOfDailyAvg ? "熱" : "冷";
            msg.append(String.format(
                "%s | %s | %s | %.1f℃ | %.1f℃ | %.1f℃\n",
                date, rain, hotCold, d.maxTemp(), d.minTemp(), avg
            ));
        }

        // 加入統計資訊區塊
        msg.append("\n**統計結果**\n");

        // 最低 3 天資訊 + 平均低溫
        msg.append("\n\t最低 3 天：\n").append(
            Arrays.stream(lowest3Dates)
                .map(d -> d.format(DATE_FMT))
                .collect(Collectors.joining(", "))
        ).append(String.format("（平均 %.1f℃）\n", avgLow));

        // 最高 3 天資訊 + 平均高溫
        msg.append("\n\t最高 3 天：\n").append(
            Arrays.stream(highest3Dates)
                .map(d -> d.format(DATE_FMT))
                .collect(Collectors.joining(", "))
        ).append(String.format("\n\t（平均 %.1f℃）\n", avgHigh));

        // 顯示連續下雨區間（若有）
        if (!rainyRuns.isEmpty()) {
            for (List<WeatherData> run : rainyRuns) {
                String from = run.get(0).date().format(DATE_FMT);
                String to = run.get(run.size() - 1).date().format(DATE_FMT);
                msg.append(String.format("\n\t 連續 5 天下雨：%s ~ %s\n", from, to));
            }
        } else {
            msg.append("\n\t☀️ 本月無連續 5 天下雨\n");
        }

        // 顯示所有週末的天氣狀況
        msg.append("\n 週末天氣：\n");
        data.stream()
            .filter(d -> {
                DayOfWeek dow = d.date().getDayOfWeek();
                return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            })
            .sorted(Comparator.comparing(WeatherData::date))
            .forEach(d -> {
                String date = d.date().format(DATE_FMT);
                String dowName = d.date().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.TAIWAN);
                String rain = d.precipitation() > 0 ? "雨" : "晴";
                msg.append(String.format("- %s %s：%s\n", date, dowName, rain));
            });

        // 發送完整報表到 Teams 頻道
        teamsNotifyService.sendMessage(msg.toString());
    }
}
