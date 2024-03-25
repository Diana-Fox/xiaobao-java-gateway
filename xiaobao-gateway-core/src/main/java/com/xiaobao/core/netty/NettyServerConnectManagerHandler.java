package com.xiaobao.core.netty;

import com.xiaobao.common.util.RemotingHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


/**
 * 连接管理的生命周期,主要就是打点日志
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {
    /**
     * 当channel注册到EventLoop
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SEVER PIPLIEN :channelRegistry {}", remoteAddr);
        super.channelRegistered(ctx);
    }

    /**
     * 当channel取消注册EventLoop
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SEVER PIPLIEN :channelUnregistered {}", remoteAddr);
        super.channelUnregistered(ctx);
    }

    /**
     * 当Channel被激活并且准备好进行I/O操作的时候
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SEVER PIPLIEN :channelActive {}", remoteAddr);
        super.channelActive(ctx);
    }

    /**
     * 当channel不再活动的时候，且不再链接他的远程节点时候被调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SEVER PIPLIEN :channelInactive {}", remoteAddr);
        super.channelInactive(ctx);
    }

    /**
     * 处理自定义事件，这里处理心跳
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                //有一段时间没收到或者发送数据
                final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.debug("NETTY SEVER PIPLIEN :userEventTriggered {}", remoteAddr);
                ctx.channel().close();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 出现异常，需要处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SEVER PIPLIEN :exceptionCaught {},cause:{}", remoteAddr,cause);
        ctx.channel().close();
    }
}
