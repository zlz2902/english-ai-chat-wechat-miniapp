package com.horzits.framework.aspectj;

import com.horzits.common.annotation.RepeatSubmit;
import com.horzits.framework.interceptor.RepeatSubmitInterceptor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class RepeatSubmitAspect {
    @Autowired
    private RepeatSubmitInterceptor tokenRepeatSubmitInterceptor;

    /**
     * 在方法执行成功后立即移除Token，允许相同操作立即再次提交
     */
    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        String token = getRequestToken(request);
        if (token != null) {
            // 立即移除Token，不等待过期
            tokenRepeatSubmitInterceptor.removeToken(token);
        }
    }

    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("X-Submit-Token");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("submitToken");
        }
        return token;
    }
}
