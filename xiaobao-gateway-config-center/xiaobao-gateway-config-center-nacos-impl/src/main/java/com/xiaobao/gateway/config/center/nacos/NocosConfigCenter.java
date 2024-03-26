package com.xiaobao.gateway.config.center.nacos;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.xiaobao.common.config.Rule;
import com.xiaobao.gateway.config.center.api.ConfigCenter;
import com.xiaobao.gateway.config.center.api.RuleChangeListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class NocosConfigCenter implements ConfigCenter {
    private static final String DATA_ID = "xiaobao-api-gateway";
    private String env;
    private String serverAddr;
    private ConfigService configService;

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;
        try {
            this.configService = NacosFactory.createConfigService(this.serverAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void subscribeRuleChange(RuleChangeListener listener) {
        try {
            //获取配置
            String config = configService.getConfig(DATA_ID, env, 5000);
            log.info("从nacos中获取配置：{}", config);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRulesChange(rules);
            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                /**
                 * 收到变化
                 */
                public void receiveConfigInfo(String configInfo) {
                    log.info("config from nacos:{}", configInfo);
                    List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRulesChange(rules);
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }
}
