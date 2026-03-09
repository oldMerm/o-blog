package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.mapper.CounterMapper;
import io.github.oldmerman.web.service.CounterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CounterServiceImpl implements CounterService {

    private final CounterMapper counterMapper;

    private final ArticleMapper articleMapper;

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void init(){
      log.info("[CounterServiceImpl]系统初始化，获取运行时间数据");
      LambdaQueryWrapper<Counter> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(Counter::getCountType, Counter.Type.RUN_TIME);
      Counter counter = counterMapper.selectOne(queryWrapper);
      if(ObjectUtils.isEmpty(counter)){
          redisTemplate.opsForValue().set(RedisPrefix.SYSTEM_RUNTIME, "1");
      }else{
          redisTemplate.opsForValue().set(RedisPrefix.SYSTEM_RUNTIME, counter.getCount().toString());
      }
    }

    /**
     * 获取网页运行时间
     *
     * @return 网页运行天数
     */
    public Integer getSystemTime() {
        return Integer.valueOf(redisTemplate.opsForValue().get(RedisPrefix.SYSTEM_RUNTIME));
    }

    /**
     * 获取相关信息五个月的增量
     *
     * @return 增量的集合
     */
    public List<Counter> getIncr(Long type) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Counter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Counter::getCountType, type)
                .ge(Counter::getCreatedAt, now.minusMonths(5).withDayOfMonth(1))
                .lt(Counter::getCreatedAt, now.withDayOfMonth(1))
                .last("LIMIT 5");
        return counterMapper.selectList(queryWrapper);
    }

    /**
     * 获取更新信息
     *
     * @return 文章渲染对象
     */
    public ArticleRenderVO getArticleUpdateInfo() {
        return articleMapper.getNewMessage();
    }

}
