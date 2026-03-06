package io.github.oldmerman.web.schedule;

import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.po.User;
import io.github.oldmerman.web.mapper.CounterMapper;
import io.github.oldmerman.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    private final CounterMapper counterMapper;

    @Scheduled(cron = "0 0 23 L * ?")
    public void updateUserCountInfo(){
        log.info("定时任务执行，统计用户和文章总数");
        List<User> users = userMapper.selectList(null);
        int userCount = users.size();
        int articleCount = 0;
        for (User user : users) {
            int article = user.getArticle();
            if(article != 0){
                articleCount += article;
            }
        }
        counterMapper.insert(Counter.builder().count((long)userCount).countType(Counter.Type.USER).build());
        counterMapper.insert(Counter.builder().count((long)articleCount).countType(Counter.Type.ARTICLE).build());
    }
}
