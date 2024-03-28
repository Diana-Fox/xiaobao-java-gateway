package com.xiaobao.core.filter;

import com.xiaobao.common.config.Rule;
import com.xiaobao.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GatewayFilterChainFactory implements FilterChainFactory {
    /**
     * 单例
     **/
    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }

    //所有过滤器都加载进来
    private Map<String, FilterInterface> processorFilterIdMap = new ConcurrentHashMap<>();

    private GatewayFilterChainFactory() {
        //用spi去加载过滤器
        ServiceLoader<FilterInterface> filters = ServiceLoader.load(FilterInterface.class);
        for (FilterInterface filter : filters) {
            Filter filterAnnotation = filter.getClass().getAnnotation(Filter.class);
            log.error("load filter sucess:class:{} id:{} order:{} name:{}", filterAnnotation.getClass(),
                    filterAnnotation.id(), filterAnnotation.order(), filterAnnotation.name());
            if (filterAnnotation != null) {
                String filterId = filterAnnotation.id();
                if (StringUtils.isEmpty(filterId)) {
                    filterId = filter.getClass().getName();
                }
                processorFilterIdMap.put(filterId, filter);
            }
        }

    }

    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        //过滤器的链,要返回的
        GatewayFilterChain chain = new GatewayFilterChain();
        //
        List<FilterInterface> filters = new ArrayList<>();
        Rule rule = ctx.getRule();
        if (rule != null) {
            Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigSet();
            Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()) {
                filterConfig = iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (StringUtils.isNotEmpty(filterId) && getFilterInfo(filterId) != null) {
                    filters.add(getFilterInfo(filterId));//拿到请求
                }
            }
            //路由发送请求的过滤器 todo
            //排序
            filters.sort(Comparator.comparingInt(FilterInterface::getOrder));
            //添加到链表中
            chain.addFilterList(filters);
        }
        return chain;
    }

    @Override
    public FilterInterface getFilterInfo(String filterId) throws Exception {
        return processorFilterIdMap.get(filterId);
    }
}
