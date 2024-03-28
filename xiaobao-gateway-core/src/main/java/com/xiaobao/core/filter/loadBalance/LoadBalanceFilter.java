package com.xiaobao.core.filter.loadBalance;

import com.alibaba.fastjson2.JSON;
import com.xiaobao.common.config.Rule;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.constants.FilterConst;
import com.xiaobao.common.enums.ResponseCode;
import com.xiaobao.common.exception.NotFoundException;
import com.xiaobao.core.context.GatewayContext;
import com.xiaobao.core.filter.Filter;
import com.xiaobao.core.filter.FilterInterface;
import com.xiaobao.core.request.GatewayRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 */
@Slf4j
@Filter(
        id = FilterConst.LOAD_BALANCE_FILTER_ID,
        name = FilterConst.LOAD_BALANCE_FILTER_NAME,
        order = FilterConst.LOAD_BALANCE_FILTER_ORDER
)
public class LoadBalanceFilter implements FilterInterface {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String uniqueId = ctx.getUniqueId();
        //获取负载均衡器
        IGatewayLoadBalanceRule gatewayLoadBalance = getLoadBalanceRule(ctx);
        //获取实例
        ServiceInstance serviceInstance = gatewayLoadBalance.choose(uniqueId);
        log.info("IP:{},端口号：{}",serviceInstance.getIp(),serviceInstance.getPort());
        GatewayRequest request = ctx.getRequest();
        if (serviceInstance!=null&&request==null){
            String host=serviceInstance.getIp()+":"+serviceInstance.getPort();
            request.setModifyHost(host);
        }else{
            log.warn("NO instance available for:{}",uniqueId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    private IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        IGatewayLoadBalanceRule loadBalanceRule = null;
        Rule rule = ctx.getRule();
        if (rule != null) {
            //获取过滤器的配置
            Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigSet();
            Rule.FilterConfig filterConfig;
            Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();//迭代过滤器的配置
            while (iterator.hasNext()) {
                filterConfig = iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (filterId.equals(FilterConst.LOAD_BALANCE_FILTER_ID)) {
                    String config = filterConfig.getConfig();
                    String strategy = FilterConst.LOAD_BALANCE_STRATEGY_RANDOM;//默认轮询策略
                    if (StringUtils.isNotEmpty(config)) {
                        Map<String, String> mapTypeMap = JSON.parseObject(config, Map.class);
                        //查看有无对应配置，无则为默认
                        strategy = mapTypeMap.getOrDefault(FilterConst.LOAD_BALANCE_KEY, strategy);
                    }
                    switch (strategy) {
                        case FilterConst.LOAD_BALANCE_STRATEGY_RANDOM:
                            loadBalanceRule = RandomLoadBalanceRule.getInstance();
                            break;
                        case FilterConst.LOAD_BALANCE_STRATEGY_RANDOM_ROBIN:
                            loadBalanceRule = RoundLoadBalanceRule.getInstance(rule.getServicedId());
                            break;
                        default:
                            log.warn("No loadBalance strategy for service:{}", strategy);
                            loadBalanceRule = RandomLoadBalanceRule.getInstance();
                            break;
                    }
                }
            }
        }
        return loadBalanceRule;
    }
}
