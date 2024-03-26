package com.xiaobao.gateway.client.support.springmvc;

import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInstance;
import com.xiaobao.common.constants.BasicConst;
import com.xiaobao.common.constants.GatewayConst;
import com.xiaobao.common.util.NetUtils;
import com.xiaobao.gateway.client.core.ApiAnnotationScanner;
import com.xiaobao.gateway.client.core.ApiPropertis;
import com.xiaobao.gateway.client.support.AbstractClientRegisterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SpringMVCClientRegisterManager
        extends AbstractClientRegisterManager implements
        ApplicationListener<ApplicationEvent>, ApplicationContextAware {
    @Autowired
    private ServerProperties serverProperties;
    //处理过的bean
    private Set<Object> set = new HashSet<Object>();
    private ApplicationContext applicationContext;

    public SpringMVCClientRegisterManager(ApiPropertis apiPropertis) {
        super(apiPropertis);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationEvent) {
            try {
                doRegisterSpringMVC();
            } catch (Exception e) {
                log.error("doRegisterSpringMVC error", e);
                throw new RuntimeException(e);
            }
            log.info("SpringMVC api stared");
        }
    }

    private void doRegisterSpringMVC() {
        Map<String, RequestMappingHandlerMapping> allRequestMapping = BeanFactoryUtils.
                beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class,
                        true, false);
        for (RequestMappingHandlerMapping handlerMapping : allRequestMapping.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> me : handlerMethods.entrySet()) {
                HandlerMethod handlerMethod = me.getValue();
                Class<?> clazz = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(clazz);
                if (set.contains(bean)) {
                    continue;
                }
                //获取服务定义信息
                ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);
                if (serviceDefinition == null) {
                    return;
                }
                serviceDefinition.setEnvType(getApiPropertis().getEnv());
                //服务实例信息
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtils.getLocalIp();
                Integer port = serverProperties.getPort();
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
                if (getApiPropertis().isGray()){
                    serviceInstance.setGray(true);
                }
                //进行注册
                register(serviceDefinition, serviceInstance);
            }
        }
    }
}
