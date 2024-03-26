package com.xiaobao.gateway.config.center.api;

public interface ConfigCenter {
    void init(String serverAddr,String env);
    void subscribeRuleChange(RuleChangeListener listener);
}
