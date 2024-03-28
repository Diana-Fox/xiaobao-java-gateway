package com.xiaobao.core.filter;

import com.xiaobao.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GatewayFilterChain {
    private List<FilterInterface> filters = new ArrayList<>();

    /**
     * 批量添加过滤器，因为我们过滤器是在规则中配置的，这个规则是在nacos中配置，可能是多个
     *
     * @param filterInterfaceList
     * @return
     */
    public GatewayFilterChain addFilterList(List<FilterInterface> filterInterfaceList) {
        this.filters.addAll(filterInterfaceList);
        return this;
    }

    /**
     * 执行过滤器
     *
     * @param ctx
     * @return
     * @throws Exception
     */
    public GatewayContext doFilter(GatewayContext ctx) throws Exception {
        if (filters.isEmpty()) {
            return ctx;
        }
        try {
            //执行过滤器链
            for (FilterInterface filter : filters) {
                filter.doFilter(ctx);
            }

        } catch (Exception e) {
            log.error("执行过滤器发生异常,异常信息：{}", e.getMessage());
            throw e;
        }
        return ctx;
    }
}
