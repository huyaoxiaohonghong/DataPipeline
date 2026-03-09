package com.antigravity.module.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志。
 * </p>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 系统模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String action() default "";
}
