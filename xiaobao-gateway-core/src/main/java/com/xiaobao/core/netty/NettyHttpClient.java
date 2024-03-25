package com.xiaobao.core.netty;

import com.xiaobao.core.config.Config;
import com.xiaobao.core.cycle.LifeCycle;
import com.xiaobao.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * 对下游的请求转发
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {
    private final Config config;
    private final EventLoopGroup eventLoopGroup;

    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(Config config, EventLoopGroup eventExecutors) {
        this.config = config;
        this.eventLoopGroup = eventExecutors;
        init();
    }

    @Override
    public void init() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();
        builder.setEventLoopGroup(eventLoopGroup).
                //设置链接超时
                        setConnectTimeout(config.getHttpConnectTimeout()).
                //设置请求超时
                        setRequestTimeout(config.getHttpRequestTimeout()).
                //设置重试次数
                        setMaxRedirects(config.getHttpMaxRequestRetry()).
                //池化分配器
                        setAllocator(PooledByteBufAllocator.DEFAULT).
                //压缩
                        setCompressionEnforced(true).
                //最大连接数
                        setMaxConnections(config.getHttpMaxConnections()).
                //客户端每个地址支持的最大连接数
                        setMaxConnectionsPerHost(config.getHttpPooledConnectionIdleTimeout()).
                //客户端空闲链接超时时间
                        setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeout());
        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
