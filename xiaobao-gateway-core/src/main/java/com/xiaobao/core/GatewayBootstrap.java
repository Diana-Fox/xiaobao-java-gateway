package com.xiaobao.core;

import com.google.common.collect.ImmutableMap;
import com.xiaobao.common.config.DynamicConfigMannager;
import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.constants.BasicConst;
import com.xiaobao.common.util.NetUtils;
import com.xiaobao.core.config.Config;
import com.xiaobao.core.config.ConfigLoader;
import com.xiaobao.core.container.Container;
import com.xiaobao.gateway.config.center.api.ConfigCenter;
import com.xiaobao.gateway.register.center.api.RegisterCenter;
import com.xiaobao.gateway.register.center.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;
import java.util.Set;

@Slf4j
public class GatewayBootstrap {
    public static void main(String[] args) {
        //1、加载网关的核心配置
        Config config = ConfigLoader.getInstance().load(args);
        //组件的初始化 netty
        //配置中心管理器初始化，链接配置中心，监听相关信息
        initAndGetConfig(config);
        //启动容器
        Container container = new Container(config);
        container.start();
        //5、连接注册中心，将注册中心实例加载进来
        final RegisterCenter registerCenter = registerAndSubscribe(config);
        //优雅关机
        //接收kill信号
        shutdownHook(config,registerCenter);
    }

    private static void shutdownHook(Config config, RegisterCenter registerCenter) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                registerCenter.deRegister(buildGatewayServiceDefinition(config),buildGatewayServiceInstance(config));
            }
        });
    }

    /**
     * 连接注册中心
     *
     * @param config
     * @return
     */
    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> registerCenters = ServiceLoader.load(RegisterCenter.class);
        RegisterCenter registerCenter = registerCenters.iterator().next();
        if (registerCenter == null) {
            log.error("not found RegisterCenter impl");
            throw new RuntimeException("not found RegisterCenter impl");
        }
        registerCenter.init(config.getRegistryAddress(), config.getEnv());
        //构建网关的服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);
        //注册
        registerCenter.register(serviceDefinition,serviceInstance);
        //订阅，发现变化则将其放到DynamicConfigManager
        registerCenter.subscribeAllService(new RegisterCenterListener() {
            @Override
            public void onChanger(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                log.info("refresh sercie and instance :{} {}",serviceDefinition.getUniqueId());
                DynamicConfigMannager mannager = DynamicConfigMannager.getInstance();
                mannager.addServiceInstance(serviceDefinition.getUniqueId(),serviceInstanceSet);
            }
        });
        return registerCenter;
    }

    /**
     * 构建服务的实例信息
     * @param config
     * @return
     */
    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        ServiceInstance instance = new ServiceInstance();
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        instance.setServiceInstanceId(localIp+ BasicConst.COLON_SEPARATOR+port);
        instance.setIp(localIp);
        instance.setPort(port);
        instance.setRegisterTime(System.currentTimeMillis());
        return instance;
    }

    /**
     * 构建服务的定义信息
     *
     * @param config
     * @return
     */
    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(ImmutableMap.of());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }

    private static void initAndGetConfig(Config config) {
        //Java SPI
        ServiceLoader<ConfigCenter> configCenters = ServiceLoader.load(ConfigCenter.class);
        ConfigCenter configCenter = configCenters.iterator().next();
        if (configCenter == null) {
            log.error("not found ConfigCenter impl");
            throw new RuntimeException("not found ConfigCenter impl");
        }
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRuleChange(rules -> {
            DynamicConfigMannager.getInstance().putAllRule(rules);
        });
    }


}
