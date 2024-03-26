package com.xiaobao.gateway.register.center.api;


import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;

public interface RegisterCenter {
    /**
     * 初始化
     *
     * @param registerAddress
     * @param evn
     */
    void init(String registerAddress, String evn);

    /**
     * 注册
     *
     * @param serviceDefinition
     * @param serviceInstance
     */
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 注销
     *
     * @param serviceDefinition
     * @param serviceInstance
     */
    void deRegister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 订阅所有变化的服务
     *
     * @param registerCenterListerner
     */

    void subscribeAllService(RegisterCenterListener registerCenterListerner);
}
