package com.xiaobao.gateway.register.center.nacos;


import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.collect.ImmutableMap;
import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.constants.GatewayConst;
import com.xiaobao.gateway.register.center.api.RegisterCenter;
import com.xiaobao.gateway.register.center.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class NacosRegisterCenter implements RegisterCenter {
    //注册地址
    private String registerAddress;
    //环境
    private String env;
    //主要维护服务实例信息
    private NamingService namingService;
    //主要维护服务定义信息
    private NamingMaintainService namingMaintainService;

    //监听器列表
    private List<RegisterCenterListener> registerCenterListeners = new CopyOnWriteArrayList<>();

    @Override
    public void init(String registerAddress, String evn) {
        this.registerAddress = registerAddress;
        this.env = evn;
        try {
            this.namingService = NamingFactory.createNamingService(this.registerAddress);
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(this.registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        //构建nacos实例信息
        Instance nacosInstance = new Instance();
        nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
        nacosInstance.setPort(serviceInstance.getPort());
        nacosInstance.setIp(serviceInstance.getIp());
        nacosInstance.setMetadata(ImmutableMap.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceInstance)));

        try {
            //注册服务
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);
            //更新服务信息
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env,
                    0, ImmutableMap.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceDefinition)));
            log.info("register {} {}", serviceDefinition, serviceInstance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deRegister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.deregisterInstance(serviceDefinition.getServiceId(), env, serviceInstance.getIp(), serviceInstance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllService(RegisterCenterListener registerCenterListener) {
        registerCenterListeners.add(registerCenterListener);
        //订阅所有的服务
        doSubscribeAllServices();
        //去定时拉取一下子
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        pool.scheduleWithFixedDelay(() -> doSubscribeAllServices(), 10, 10, TimeUnit.SECONDS);
    }

    private void doSubscribeAllServices() {
        try {
            //已经订阅的服务
            Set<String> subscribe = namingService.getSubscribeServices().stream()
                    .map(ServiceInfo::getName).collect(Collectors.toSet());
            int pageNo = 1;
            int pageSize = 100;
            //nacos事件监听器
            EventListener eventListener = new NacosRegisterListener();
            //分页从nacos获取服务列表
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();
            while (CollectionUtils.isNotEmpty(serviceList)) {
                log.info("server list size:{}", serviceList.size());
                for (String service : serviceList) {
                    if (subscribe.contains(service)) {
                        continue;
                    }
                    namingService.subscribe(service,env, eventListener);
                    log.info("subscribe:{} {}", service, env);
                }
                serviceList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * nacos注册事件
     */
    public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                //实例名称
                String serviceName = namingEvent.getServiceName();
                try {
                    //获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, env);
                    //获取元数据信息
                    ServiceDefinition serviceDefinition = JSON.parseObject(service.getMetadata().
                            get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);
                    //获取实例信息
                    List<Instance> instances = namingService.getAllInstances(serviceName, env);
                    HashSet<ServiceInstance> set = new HashSet<>();
                    for (Instance instance : instances) {
                        ServiceInstance serviceInstance = JSON.parseObject(instance.getMetadata().
                                get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        set.add(serviceInstance);
                    }
                    registerCenterListeners.stream().forEach(l->l.onChanger(serviceDefinition,set));
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
