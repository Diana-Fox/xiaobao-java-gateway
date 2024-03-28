package com.xiaobao.core.filter;

import com.xiaobao.core.context.GatewayContext;

/**
 * 过滤器工厂接口
 */
public interface FilterChainFactory {
    /**
     * 构建过滤器链
     * @param ctx
     * @return
     * @throws Exception
     */
    GatewayFilterChain buildFilterChain(GatewayContext ctx)throws Exception;

    /**
     * 通过过滤器Id获取过滤器
     * @param filterId
     * @return
     * @throws Exception
     */
    FilterInterface getFilterInfo(String filterId)throws Exception;
}
