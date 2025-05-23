package job;

import service.WeatherAnalysisService;

import java.time.YearMonth;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class WeatherJob implements Job {

    private final WeatherAnalysisService analysisService;

    public WeatherJob(WeatherAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        // 以當前月為例
        YearMonth ym = YearMonth.now(); 
        analysisService.reportAndStats(YearMonth.now());

    }
}
