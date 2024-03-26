package com.xiaobao.gateway.client.support;

import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.gateway.client.core.ApiPropertis;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.xiaobao.gateway.register.center.api.RegisterCenter;

import java.util.ServiceLoader;


@Slf4j
public class AbstractClientRegisterManager {
    @Getter
    private ApiPropertis apiPropertis;
    /**
     * 读取实现类
     */
    private RegisterCenter registerCenter;

    protected AbstractClientRegisterManager(ApiPropertis apiPropertis) {
        this.apiPropertis = apiPropertis;
        //利用啊Java的SPI机制初始化注册中心对象
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        registerCenter = serviceLoader.iterator().next();
        //初始化
        registerCenter.init(apiPropertis.getRegisterAddress(), apiPropertis.getEnv());
    }

    /**
     * 注册
     * @param serviceDefinition
     * @param serviceInstance
     */
    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        registerCenter.register(serviceDefinition, serviceInstance);
    }
}
