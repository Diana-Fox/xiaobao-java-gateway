package com.xiaobao.core.filter;

import java.lang.annotation.*;

/**
 * 过滤器注解类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {
    /**
     * 过滤器id
     * @return
     */
    String id();

    /**
     * 过滤器名称
     * @return
     */
    String name()default "";

    /**
     * 排序
     * @return
     */
    int order() default 0;
}
