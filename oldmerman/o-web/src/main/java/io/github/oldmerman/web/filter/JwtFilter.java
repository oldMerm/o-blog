package io.github.oldmerman.web.filter;

import io.github.oldmerman.common.enums.JwtEnum;
import io.github.oldmerman.web.util.JwtUtil;
import io.github.oldmerman.web.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(1)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    private static final Set<String> SKIP = Set.of(
            "/auth/login",
            "/auth/register");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if(SKIP.contains(req.getRequestURL().toString())){
            chain.doFilter(req, res);
            return;
        }
        String authorization = req.getHeader(JwtEnum.AUTHORIZATION.getValue());
        try {
            String token = authorization.substring(JwtEnum.AUTH_PREFIX.getValue().length());
            String id = jwtUtil.parseToken(token).getSubject();
            UserContext.setUserId(Long.valueOf(id));
            chain.doFilter(req, res);
        } finally {
            UserContext.clear();
        }

    }


}
