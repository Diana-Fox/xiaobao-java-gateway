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
import java.util.concurrent.ThreadLocalRandom;

/**
 * 负载均衡————随机选择
 */
@Slf4j
public class RandomLoadBalanceRule implements IGatewayLoadBalanceRule {
    private static class SingletonInstance {
        private static final RandomLoadBalanceRule INSTANCE = new RandomLoadBalanceRule();
    }

    private RandomLoadBalanceRule() {
    }

    public static RandomLoadBalanceRule getInstance() {
        return SingletonInstance.INSTANCE;
    }

    @Override
    public ServiceInstance choose(GatewayContext gatewayContext) {
        String uniqueId = gatewayContext.getUniqueId();
        return choose(uniqueId);
    }

    @Override
    public ServiceInstance choose(String uniqueId) {
        //获取了
        Set<ServiceInstance> serviceInstances =
                DynamicConfigMannager.getInstance().getServiceInstanceByUniquedId(uniqueId);
        if (serviceInstances.isEmpty()) {
            log.error("no instance available for :{}", uniqueId);
            throw new NotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
        }
        List<ServiceInstance> instances = new ArrayList<>(serviceInstances);
        int i = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(i);
    }
}
