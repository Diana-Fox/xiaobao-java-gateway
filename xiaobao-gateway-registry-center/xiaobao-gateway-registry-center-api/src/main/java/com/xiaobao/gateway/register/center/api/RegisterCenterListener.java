package com.xiaobao.gateway.register.center.api;

import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;

import java.util.Set;

public interface RegisterCenterListener {
    /**
     * 修改
     * @param serviceDefinition
     * @param serviceInstanceSet
     */
    void onChanger(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);
}
