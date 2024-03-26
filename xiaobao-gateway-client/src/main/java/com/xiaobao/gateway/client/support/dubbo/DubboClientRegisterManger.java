package com.xiaobao.gateway.client.support.dubbo;

import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.constants.BasicConst;
import com.xiaobao.common.constants.GatewayConst;
import com.xiaobao.common.util.NetUtils;
import com.xiaobao.gateway.client.core.ApiAnnotationScanner;
import com.xiaobao.gateway.client.core.ApiPropertis;
import com.xiaobao.gateway.client.support.AbstractClientRegisterManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class DubboClientRegisterManger
        extends AbstractClientRegisterManager
        implements ApplicationListener<ApplicationEvent> {
    private Set<Object> set = new HashSet<Object>();

    public DubboClientRegisterManger(ApiPropertis apiPropertis) {
        super(apiPropertis);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ServiceBeanExportedEvent) {
            //拿到注册信息
            ServiceBean serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
            try {
                //注册dubbo服务
                doRegisterDubbo(serviceBean);

            } catch (Exception e) {
                log.error("doRegisterDubbo error", e);
                throw new RuntimeException(e);
            }
        } else if (applicationEvent instanceof ApplicationStartedEvent) {
            log.info("dubbo api started");
        }
    }

    private void doRegisterDubbo(ServiceBean serviceBean) {
        Object bean = serviceBean.getRef();
        if (set.contains(bean)) {
            return;
        }
        //去拿dubbo服务定义信息
        ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean, serviceBean);
        if (serviceDefinition == null) {
            return;
        }
        serviceDefinition.setEnvType(getApiPropertis().getEnv());
        //服务实例信息
        ServiceInstance serviceInstance = new ServiceInstance();
        String localIp = NetUtils.getLocalIp();
        Integer port = serviceBean.getProtocol().getPort();
        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
        String uniqueId = serviceDefinition.getUniqueId();
        String version = serviceDefinition.getVersion();

        serviceInstance.setUniqueId(uniqueId);
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(System.currentTimeMillis());
        serviceInstance.setVersion(version);
        serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);
        //进行注册
        register(serviceDefinition, serviceInstance);
    }
}
