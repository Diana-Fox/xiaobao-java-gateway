package com.xiaobao.gateway.client.core;

import com.xiaobao.common.config.DubboServiceInvoker;
import com.xiaobao.common.config.HttpServiceInvoker;
import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.config.ServiceInvoker;
import com.xiaobao.common.constants.BasicConst;
import com.xiaobao.gateway.client.support.dubbo.DubboConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解扫描类
 */
public class ApiAnnotationScanner {
    /****************单例******************/
    private ApiAnnotationScanner() {
    }

    private static class SingletonHolder {
        static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }

    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ServiceDefinition scanner(Object bean, Object... args) {
        Class<?> aClass = bean.getClass();
        if (!aClass.isAnnotationPresent(ApiService.class)) {
            return null;
        }
        //获取注解信息
        ApiService apiService = aClass.getAnnotation(ApiService.class);
        String serviceId = apiService.serviceId();
        ApiProtocol protocol = apiService.protocol();
        String patternPath = apiService.patternPath();
        String version = apiService.version();
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();
        Method[] methods = aClass.getMethods();
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
                if (apiInvoker == null) {
                    continue;
                }
                String path = apiInvoker.path();
                switch (protocol) {
                    case HTTP:
                        //创建http调用信息
                        HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                        invokerMap.put(path, httpServiceInvoker);
                        break;
                    case DUBBO:
                        ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                        DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                        String dubboVersion = dubboServiceInvoker.getVersion();
                        if (StringUtils.isNoneBlank(dubboVersion)) {
                            version = dubboVersion;
                        }
                        invokerMap.put(path, dubboServiceInvoker);
                        break;
                    default:
                        break;
                }
            }
            //创建服务定义信息
            ServiceDefinition serviceDefinition = new ServiceDefinition();
            serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            serviceDefinition.setVersion(version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patternPath);
            serviceDefinition.setEnable(true);
            return serviceDefinition;
        }
        return null;
    }


    /**
     * 构建HTTPServiceInvoker对象
     *
     * @param path
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path) {
        HttpServiceInvoker invoker = new HttpServiceInvoker();
        invoker.setInvokerPath(path);
        return invoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     *
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        //就是解析serviceBean上的信息
        DubboServiceInvoker invoker = new DubboServiceInvoker();
        invoker.setInvokerPath(path);
        String name = method.getName();
        String address = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();
        invoker.setRegisterAddress(address);
        invoker.setMethodName(name);
        invoker.setInterfaceClass(interfaceClass);
        //参数集合
        String[] paramterType = new String[method.getParameterCount()];
        //对应class
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            paramterType[i] = classes[i].getName();
        }
        //
        invoker.setParameterTypes(paramterType);
        Integer serviceTimeout = serviceBean.getTimeout();
        //如果没有设置超时时间
        if (serviceTimeout == null || serviceTimeout.intValue() == 0) {
            serviceTimeout = DubboConstants.DUBBO_TIMEOUT;
        }
        invoker.setTimeout(serviceTimeout);
        String version = serviceBean.getVersion();
        invoker.setVersion(version);
        return invoker;
    }


}
