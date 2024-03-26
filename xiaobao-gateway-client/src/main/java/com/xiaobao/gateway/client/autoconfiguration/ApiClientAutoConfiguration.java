package com.xiaobao.gateway.client.autoconfiguration;

import com.xiaobao.gateway.client.core.ApiPropertis;
import com.xiaobao.gateway.client.support.dubbo.DubboClientRegisterManger;
import com.xiaobao.gateway.client.support.springmvc.SpringMVCClientRegisterManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * 自动装配,配置了这些就启动
 */
@Configuration
@EnableConfigurationProperties(ApiPropertis.class)
@ConditionalOnProperty(prefix = "api", name = {"registerAddress"})
public class ApiClientAutoConfiguration {
    @Autowired
    private ApiPropertis apiPropertis;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegisterManager.class)
    public SpringMVCClientRegisterManager springMVCClientRegisterManager() {
        return new SpringMVCClientRegisterManager(apiPropertis);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(DubboClientRegisterManger.class)
    public DubboClientRegisterManger dubboClientRegisterManger() {
        return new DubboClientRegisterManger(apiPropertis);
    }
}
