package com.xiaobao.core.filter;

import com.xiaobao.core.context.GatewayContext;

/**
 * 过滤器顶级接口
 */
public interface FilterInterface {
    void doFilter(GatewayContext ctx) throws Exception;

    /**
     * 执行顺序
     *
     * @return
     */
    default int getOrder() {
        Filter filter = this.getClass().getAnnotation(Filter.class);//去拿注解
        if (filter==null){
            return filter.order();
        }
        return Integer.MAX_VALUE;
    }
}
