package com.xiaobao.common.config;

import lombok.Data;

import java.util.Objects;


/**
 * 服务调用接口模式描述
 */
public interface ServiceInvoker {
    /**
     * 获取真正的服务调用的全路径
     *
     * @return
     */
    String getInvoker();

    void setInvokerPath(String invokerPath);

    /**
     * 超时时间
     *
     * @return
     */
    int getTimeout();

    /**
     * 获取调用该服务或者方法的超时时间
     *
     * @param timeout
     */
    void setTimeout(int timeout);
}
