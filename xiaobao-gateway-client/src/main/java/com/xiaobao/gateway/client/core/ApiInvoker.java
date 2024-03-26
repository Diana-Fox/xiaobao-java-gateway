package com.xiaobao.gateway.client.core;

import java.lang.annotation.*;

/**
 * 必须写到方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();
}
