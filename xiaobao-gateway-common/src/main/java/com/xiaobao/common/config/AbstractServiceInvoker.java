package com.xiaobao.common.config;

/**
 * 抽象的服务调用接口实现
 */
public class AbstractServiceInvoker implements ServiceInvoker {
    /**
     *
     */
    protected String invokerPath;
    protected int timeout = 5000;

    @Override
    public String getInvoker() {
        return invokerPath;
    }

    @Override
    public void setInvokerPath(String invokerPath) {
        invokerPath = invokerPath;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        timeout = timeout;
    }
}
