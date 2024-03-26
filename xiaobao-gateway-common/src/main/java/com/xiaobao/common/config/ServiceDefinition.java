package com.xiaobao.common.config;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 资源服务定义类
 */
@Builder
@Data
public class ServiceDefinition implements Serializable {
    private static final long serialVersionUID = 5476554107339639674L;
    /**
     * 唯一服务ID
     */
    private String uniqueId;
    /**
     * 服务Id
     */
    private String serviceId;

    /**
     * 服务版本号
     */
    private String version;
    /**
     * 服务具体的协议 http或者dubbo协议
     */
    private String protocol;

    /**
     * 服务调用的路径
     */
    private String patternPath;
    /**
     * 环境名称
     */
    private String envType;
    /**
     * 服务启动禁用
     */
    private boolean enable = true;
    /**
     * 可调用的路径
     */
    private Map<String/*invokerPath*/, ServiceInvoker> invokerMap;

    public ServiceDefinition() {
    }

    public ServiceDefinition(String uniqueId, String serviceId, String version,
                             String protocol, String patternPath, String envType,
                             boolean enable, Map<String, ServiceInvoker> invokerMap) {
        this.uniqueId = uniqueId;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.patternPath = patternPath;
        this.envType = envType;
        this.enable = enable;
        this.invokerMap = invokerMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDefinition that = (ServiceDefinition) o;

        /**
         * 只比较服务Id就行了
         */
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
