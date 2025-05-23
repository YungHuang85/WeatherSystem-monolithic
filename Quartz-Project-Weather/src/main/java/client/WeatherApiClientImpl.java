package client;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import dto.MonthlyWeatherResponse;
import dto.WeatherData;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Primary
@Component
public class WeatherApiClientImpl implements WeatherApiClient {

    private final WebClient client;

    public WeatherApiClientImpl(WebClient client) {
        this.client = client;
    }

    @Override
    public List<WeatherData> fetchMonthlyData(YearMonth ym, double lat, double lon) {
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate   = LocalDate.now().isBefore(ym.atEndOfMonth())
                             ? LocalDate.now()
                             : ym.atEndOfMonth();

        String uri = "/v1/forecast"
            + "?latitude="   + lat
            + "&longitude="  + lon
            + "&daily=temperature_2m_min,temperature_2m_max,precipitation_sum"
            + "&start_date=" + startDate
            + "&end_date="   + endDate
            + "&timezone=Asia/Taipei";

        System.out.println("[WeatherApiClient] 呼叫 URI：" + uri);
        try {
            MonthlyWeatherResponse resp = client.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(MonthlyWeatherResponse.class)
                .block();

            List<WeatherData> list = new ArrayList<>();
            for (int i = 0; i < resp.daily.time.size(); i++) {
                list.add(new WeatherData(
                    LocalDate.parse(resp.daily.time.get(i)),
                    resp.daily.temperature_2m_min.get(i),
                    resp.daily.temperature_2m_max.get(i),
                    resp.daily.precipitation_sum.get(i)
                ));
            }
            return list;

        } catch (WebClientResponseException e) {
            System.err.println("[WeatherApiClient] HTTP " + e.getRawStatusCode());
            System.err.println("[WeatherApiClient] " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
