package io.github.oldmerman.web.aspect;

import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.web.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireAdminAspect {

    private final UserAdminService service;

    @Pointcut("@annotation(io.github.oldmerman.common.annotation.RequireAdmin)")
    public void adminRequirePointcut(){}

    @Around("adminRequirePointcut()")
    public Object checkUserPermission(ProceedingJoinPoint jp) throws Throwable{
        if(service.getUserPermission().equals("admin")) {
            return jp.proceed();
        }else{
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

}
