package com.xiaobao.common.config;

/**
 * dubbo协议的注册服务间调用模型类
 */
public class DubboServiceInvoker extends AbstractServiceInvoker {
    /**
     * 注册地址
     */
    private String registerAddress;
    /**
     * 接口全类名
     */
    private String interfaceClass;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 参数名称的集合
     */
    private String[] parameterTypes;
    /**
     * 服务版本
     */
    private String version;

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public String getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(String interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
