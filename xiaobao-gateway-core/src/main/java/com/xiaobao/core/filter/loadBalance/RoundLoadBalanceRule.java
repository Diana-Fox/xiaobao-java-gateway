package com.xiaobao.core.filter.loadBalance;

import com.xiaobao.common.config.DynamicConfigMannager;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.enums.ResponseCode;
import com.xiaobao.common.exception.NotFoundException;
import com.xiaobao.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡——轮询
 */
@Slf4j
public class RoundLoadBalanceRule implements IGatewayLoadBalanceRule {
    private String uniqueId;
    private static ConcurrentHashMap<String, RandomLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();

    //选中的
    private AtomicInteger position = new AtomicInteger();

    public RoundLoadBalanceRule(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public static RandomLoadBalanceRule getInstance(String serviceId) {
        RandomLoadBalanceRule randomLoadBalanceRule = serviceMap.get(serviceId);
        if (randomLoadBalanceRule == null) {
            serviceMap.put(serviceId, randomLoadBalanceRule);
        }
        return randomLoadBalanceRule;
    }

    @Override
    public ServiceInstance choose(GatewayContext gatewayContext) {
        return choose(gatewayContext.getUniqueId());
    }

    @Override
    public ServiceInstance choose(String uniqueId) {
        Set<ServiceInstance> serviceInstances =
                DynamicConfigMannager.getInstance().getServiceInstanceByUniquedId(uniqueId);
        if (serviceInstances.isEmpty()) {
            log.error("no instance available for :{}", uniqueId);
            throw new NotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
        }
        List<ServiceInstance> instances = new ArrayList<>(serviceInstances);
        int post = Math.abs(this.position.incrementAndGet());
        return instances.get(post);
    }
}
