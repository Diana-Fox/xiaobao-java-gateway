package com.xiaobao.core.filter.loadBalance;

import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.core.context.GatewayContext;

/**
 * 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {
    /**
     * 通过上下文选择实例
     *
     * @param gatewayContext
     * @return
     */
    ServiceInstance choose(GatewayContext gatewayContext);

    /**
     * 通过服务名称获取实例
     *
     * @param   uniqueId
     * @return
     */
    ServiceInstance choose(String   uniqueId);
}
