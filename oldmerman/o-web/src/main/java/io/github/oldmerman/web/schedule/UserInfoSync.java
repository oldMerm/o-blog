package io.github.oldmerman.web.schedule;

import io.github.oldmerman.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author oldmerman
 *
 * 同步用户的数据
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserInfoSync {

    private final UserMapper userMapper;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateUserArticleInfo(){
        log.info("定时任务执行，同步用户文章点赞数据");
        userMapper.updateUserArticle();
    }
}
