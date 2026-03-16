package com.horzits.framework.interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.horzits.common.annotation.RepeatSubmit;
import com.horzits.common.core.domain.AjaxResult;
import com.horzits.common.utils.ServletUtils;

/**
 * 防止重复提交拦截器
 *
 * @author ruoyi
 */
@Component
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor
{
    /**
     * Token缓存：key = token，value = 当前时间戳
     */
    private final Map<String, Long> tokenCache = new ConcurrentHashMap<>();

    /**
     * 清理缓存间隔时间: 10 分钟清理一次过期token
     */
    private static final long CLEAN_INTERVAL = 10 * 60 * 1000L;
    private long lastCleanTime = System.currentTimeMillis();// 上次清理时间
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null)
            {
                if (this.isRepeatSubmit(request, annotation))
                {
                    AjaxResult ajaxResult = AjaxResult.error(annotation.message());
                    ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

    /**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request 请求信息
     * @param annotation 防重复注解参数
     * @return 结果
     * @throws Exception
     */
    public  boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation){
        // 获取请求中的Token
        String token = getRequestToken(request);
        if (token == null || token.isEmpty()) {
            return false; // 没有Token不进行重复提交验证
        }

        long currentTime = System.currentTimeMillis();

        // 定期清理过期Token
        if (currentTime - lastCleanTime > CLEAN_INTERVAL) {
            cleanExpiredTokens(annotation.interval());
            lastCleanTime = currentTime;
        }

        // 检查Token是否在有效期内
        Long lastSubmitTime = tokenCache.get(token);
        if (lastSubmitTime != null && (currentTime - lastSubmitTime) < annotation.interval()) {
            return true; // 重复提交
        }

        // 记录本次提交
        tokenCache.put(token, currentTime);
        return false;
    }

    /**
     * 从请求中获取Token
     */
    private String getRequestToken(HttpServletRequest request) {
        // 优先从Header中获取
        String token = request.getHeader("X-Submit-Token");
        if (token == null || token.isEmpty()) {
            // 从参数中获取
            token = request.getParameter("submitToken");
        }
        return token;
    }

    /**
     * 清理过期Token
     */
    private void cleanExpiredTokens(int interval) {
        long currentTime = System.currentTimeMillis();
        tokenCache.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > interval * 2L
        );
    }

    /**
     * 手动移除Token（用于业务处理完成后立即释放）
     */
    public void removeToken(String token) {
        tokenCache.remove(token);
    }



}
