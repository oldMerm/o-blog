package io.github.oldmerman.web.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.web.mapper.CounterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemTimeUpdater {

    private final CounterMapper counterMapper;

    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 6 * * ?")
    public void updateSystemTime(){
        Long incr = redisTemplate.opsForValue().increment(RedisPrefix.SYSTEM_RUNTIME);
        LambdaUpdateWrapper<Counter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Counter::getCountType, Counter.Type.RUN_TIME).set(Counter::getCount, incr);
        counterMapper.update(updateWrapper);
    }
}
