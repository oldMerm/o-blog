package io.github.oldmerman.web.schedule;

import io.github.oldmerman.common.constant.RedisPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemTimeUpdater {

    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 6 * * ?")
    public void updateSystemTime(){
        redisTemplate.opsForValue().increment(RedisPrefix.SYSTEM_RUNTIME);
    }
}
