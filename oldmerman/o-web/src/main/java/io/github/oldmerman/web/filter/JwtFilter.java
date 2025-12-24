package io.github.oldmerman.web.filter;

import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.util.JwtUtil;
import io.github.oldmerman.web.util.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(1)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final Set<String> SKIP = Set.of(
            "/auth/**");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String servletPath = req.getServletPath();
        if (SKIP.stream().anyMatch(p -> MATCHER.match(p, servletPath))) {
            chain.doFilter(request, response);
            return;
        }
        String authorization = req.getHeader(WebEnum.AUTHORIZATION.getValue());
        try {
            String token = authorization.substring(WebEnum.AUTH_PREFIX.getValue().length());
            String id = jwtUtil.parseToken(token).getSubject();
            UserContext.setUserId(Long.valueOf(id));
            chain.doFilter(req, res);
        } finally {
            UserContext.clear();
        }

    }


}
