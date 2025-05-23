package quartz_project_weather_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"aspect","client","job","config","controller","service","dto"})
public class QuartzProjectWeatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuartzProjectWeatherApplication.class, args);
	}

}
