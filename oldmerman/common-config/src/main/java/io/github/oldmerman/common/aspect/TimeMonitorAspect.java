package io.github.oldmerman.common.aspect;

import io.github.oldmerman.common.util.MethodStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeMonitorAspect {

    private final MethodStatistics statistics;

    @Pointcut("@annotation(io.github.oldmerman.common.annotation.TimeMonitor)")
    public void timeMonitorPointcut(){}

    @Around("timeMonitorPointcut()")
    public Object textMethodTimeAndRecord(ProceedingJoinPoint jp) throws Throwable{
        String methodName = jp.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            return jp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            statistics.record(methodName, duration);
            MethodStatistics.MethodStats stats = statistics.getStats(methodName);
            log.info("方法统计 - {}: 本次耗时={}ms 平均耗时={}ms 调用次数={}",
                    methodName, duration, String.format("%.2f",stats.getAverageTime()), stats.getCount());
        }
    }

}
