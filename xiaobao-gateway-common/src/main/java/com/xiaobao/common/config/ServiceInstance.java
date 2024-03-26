package com.xiaobao.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 一个服务对应多个实例
 */
@Data
public class ServiceInstance implements Serializable {
    private static final long serialVersionUID = -6088561437491674107L;

    public ServiceInstance() {
    }

    /**
     * 服务实例：ip:port
     */
    protected String serviceInstanceId;
    /**
     * 服务定义唯一id：uniqueId
     */
    protected String uniqueId;
    /**
     * 服务实力地址：ip:port
     */
    protected String ip;
    private int port;
    /**
     * 标签信息
     */
    protected String tags;
    /**
     * 权重信息
     */
    private Integer weight;
    /**
     * 服务注册信息
     */
    private long registerTime;

    /**
     * 实例实例启用禁用
     */
    protected boolean enable = true;
    /**
     * 版本号
     */
    protected String version;
    /**
     * 灰度
     */
    protected boolean gray;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }
}
