package io.github.oldmerman.web.filter;

import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.JwtUtil;
import io.github.oldmerman.web.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(1)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate redisTemplate;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final Set<String> SKIP = Set.of(
            "/auth/login","/auth/captcha","/auth/email","/auth/register","/auth/refresh","/article/public/info");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if(req.getMethod().equals("OPTIONS")){
            chain.doFilter(request, response);
            return;
        }
        String servletPath = req.getServletPath();
        if (SKIP.stream().anyMatch(p -> MATCHER.match(p, servletPath))) {
            chain.doFilter(request, response);
            return;
        }
        String authorization = req.getHeader(WebEnum.AUTHORIZATION.getValue());
        try {
            String token = authorization.substring(WebEnum.AUTH_PREFIX.getValue().length());
            if("1".equals(redisTemplate.opsForValue().get(RedisPrefix.BLACK_TOKEN + token))){
                throw new BusinessException(BusErrorCode.TOKEN_EXPIRED);
            }
            Claims claims = jwtUtil.parseToken(token);
            String id = claims.getSubject();
            if(claims.getExpiration().getTime() < System.currentTimeMillis() +
                    NumEnum.ALLOW_ACCESS_TIME.getValue() * 1800){
                throw new BusinessException(BusErrorCode.TOKEN_EXPIRED);
            }
            UserContext.setUserId(Long.valueOf(id));
            chain.doFilter(req, res);
        } finally {
            UserContext.clear();
        }

    }


}
