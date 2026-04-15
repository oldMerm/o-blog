package io.github.oldmerman.web.interceptor;

import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.util.UserContext;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        String auth = redisTemplate.opsForValue().get(RedisPrefix.ADMIN_CACHE + userId);
        Byte i;

        if (!StringUtils.hasText(auth)) {
            i = userMapper.isValidAuthToken(userId);
            if (i == null || i != 1) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            redisTemplate.opsForValue().set(
                    RedisPrefix.ADMIN_CACHE + userId,
                    "1",
                    1,
                    TimeUnit.HOURS
            );
            return true;
        }

        if (!"1".equals(auth)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return true;
    }

}