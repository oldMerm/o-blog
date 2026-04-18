package io.github.oldmerman.web.interceptor;

import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.common.util.JwtUtil;
import io.github.oldmerman.web.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    // 完全放行：不校验 token，也不设置上下文
    private static final Set<String> SKIP = Set.of(
            "/auth/login", "/auth/captcha", "/auth/email", "/auth/register", "/auth/refresh",
            "/counter/newArt", "/counter/health",
            "/version", "/version/*",
            "/agent/chat/stream"
    );

    // 公共路径：未登录可访问，有 token 则解析并设置上下文
    private static final Set<String> PUBLIC = Set.of(
            "/article/public/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        String servletPath = request.getServletPath();

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 完全放行路径
        if (SKIP.stream().anyMatch(p -> MATCHER.match(p, servletPath))) {
            return true;
        }

        String authorization = request.getHeader(WebEnum.AUTHORIZATION.getValue());

        // 公共路径：无 token 直接放行，有 token 继续校验
        if (PUBLIC.stream().anyMatch(p -> MATCHER.match(p, servletPath))) {
            if (!StringUtils.hasText(authorization)) {
                return true;
            }
        }

        // 受保护路径必须有 token
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authorization.substring(WebEnum.AUTH_PREFIX.getValue().length());

        // 检查黑名单
        if ("1".equals(redisTemplate.opsForValue().get(RedisPrefix.BLACK_TOKEN + token))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 解析 token
        Claims claims = jwtUtil.parseToken(token);
        String id = claims.getSubject();

        // 检查是否即将过期（续期逻辑）
        if (claims.getExpiration().getTime() < System.currentTimeMillis() +
                NumEnum.ALLOW_ACCESS_TIME.getValue()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 设置用户上下文
        UserContext.setUserId(Long.valueOf(id));

        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request,
                                @Nonnull HttpServletResponse response,
                                @Nonnull Object handler,
                                @Nonnull Exception ex) {
        // 请求结束后清理上下文，避免线程池复用导致数据泄露
        UserContext.clear();
    }
}