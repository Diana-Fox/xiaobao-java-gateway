package com.xiaobao.core;

import com.xiaobao.core.config.Config;
import com.xiaobao.core.config.ConfigLoader;
import com.xiaobao.core.container.Container;

public class GatewayBootstrap {
    public static void main(String[] args) {
        //1、加载网关的核心配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());
        //组件的初始化 netty
        //配置中心管理器初始化，链接配置中心，监听相关信息
        //启动容器
        Container container = new Container(config);
        container.start();
        //将注册中心实例加载进来
        //优雅关机
    }
}
