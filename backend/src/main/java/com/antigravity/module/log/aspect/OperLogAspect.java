package com.antigravity.module.log.aspect;

import com.antigravity.module.log.annotation.OperLog;
import com.antigravity.module.log.entity.OperationLog;
import com.antigravity.module.log.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截标注了 {@link OperLog} 的 Controller 方法，自动采集请求参数、执行耗时、异常信息
 * 并异步写入操作日志表。
 * </p>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(com.antigravity.module.log.annotation.OperLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        // 构建操作日志
        OperationLog logEntity = new OperationLog();
        logEntity.setModule(operLog.module());
        logEntity.setAction(operLog.action());
        logEntity.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName() + "()");

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logEntity.setIpAddress(getClientIp(request));
            // 从 JWT Filter 设置的 attribute 获取用户名
            Object username = request.getAttribute("username");
            if (username != null) {
                logEntity.setUsername(username.toString());
            }
        }

        // 记录请求参数（截断以防止参数过大）
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = Arrays.toString(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...(truncated)";
                }
                logEntity.setParams(params);
            }
        } catch (Exception e) {
            logEntity.setParams("参数序列化失败");
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            logEntity.setStatus(1);
        } catch (Throwable ex) {
            logEntity.setStatus(0);
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000) + "...(truncated)";
            }
            logEntity.setErrorMsg(errorMsg);
            throw ex;
        } finally {
            logEntity.setDuration(System.currentTimeMillis() - startTime);
            // 异步保存日志
            try {
                operationLogService.asyncRecordOperationLog(logEntity);
            } catch (Exception e) {
                log.error("操作日志记录失败", e);
            }
        }

        return result;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
