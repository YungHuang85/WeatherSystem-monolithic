package aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class WeatherAspect {

    @Pointcut("execution(* service.WeatherAnalysisService.reportAndStats(..))")
    public void reportAndStatsPointcut() {}

    @Before("reportAndStatsPointcut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("👉 即將執行分析：" + joinPoint.getSignature());
    }

    @AfterReturning("reportAndStatsPointcut()")
    public void after() {
        System.out.println("✅ 分析完成");
    }

    @AfterThrowing(value = "reportAndStatsPointcut()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        System.out.println("❌ 執行錯誤：" + ex.getMessage());
    }
}
