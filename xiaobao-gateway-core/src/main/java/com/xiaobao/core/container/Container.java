package com.xiaobao.core.container;

import com.xiaobao.core.config.Config;
import com.xiaobao.core.cycle.LifeCycle;
import com.xiaobao.core.netty.NettyHttpClient;
import com.xiaobao.core.netty.NettyHttpServer;
import com.xiaobao.core.netty.processor.NettyCoreProcessor;
import com.xiaobao.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Container implements LifeCycle {
    private final Config config;
    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;
    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        this.nettyProcessor = new NettyCoreProcessor();
        //用同一个线程池
        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventExecutorsWork());
    }

    @Override
    public void start() {
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("api gateway started!");
    }

    @Override
    public void shutdown() {
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
