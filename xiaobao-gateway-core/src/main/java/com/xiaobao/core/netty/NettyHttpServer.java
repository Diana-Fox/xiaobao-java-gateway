package com.xiaobao.core.netty;

import com.xiaobao.common.util.RemotingUtil;
import com.xiaobao.core.config.Config;
import com.xiaobao.core.cycle.LifeCycle;
import com.xiaobao.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHttpServer implements LifeCycle {
    /**
     * 引入启动的配置类
     */
    private final Config config;
    /**
     * 启动类
     */
    private ServerBootstrap serverBootstrap;

    /**
     * boss线程池
     */
    private EventLoopGroup eventExecutorsBoss;
    /**
     * 工作线程池
     */
    @Getter
    private EventLoopGroup eventExecutorsWork;

    private final NettyProcessor processor;

    public NettyHttpServer(Config config,NettyProcessor processor) {
        this.processor = processor;
        this.config = config;
        init();
    }


    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.eventExecutorsBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-nio"));//
            this.eventExecutorsWork = new EpollEventLoopGroup(config.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("netty-work-nio"));
        } else {
            this.eventExecutorsBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-nio"));//
            this.eventExecutorsWork = new NioEventLoopGroup(config.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("netty-work-nio"));
        }
    }

    /**
     * linux系统，并且支持epoll
     *
     * @return
     */
    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        this.serverBootstrap.
                group(eventExecutorsBoss, eventExecutorsWork).
                channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class).
                localAddress(new InetSocketAddress(config.getPort())).
                childHandler(
                        new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(new HttpServerCodec(),//http解码器
                                        new HttpObjectAggregator(config.getMaxContentLength()),//请求报文聚合成FullHttpRequest
                                        new NettyServerConnectManagerHandler(),
                                        new NettyHttpServerHandler(processor));
                            }
                        });
        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port:{}", this.config.getPort());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        //优雅停机
        if (this.eventExecutorsBoss != null) {
            eventExecutorsBoss.shutdownGracefully();
        }
        if (this.eventExecutorsWork != null) {
            eventExecutorsWork.shutdownGracefully();
        }
    }
}
