package com.xiaobao.core.netty;

import com.xiaobao.core.context.HttpRequestWrapper;
import com.xiaobao.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 核心类
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
//public class NettyHttpServerHandler extends SimpleChannelInboundHandler {
    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        HttpRequestWrapper wrapper = new HttpRequestWrapper();
        wrapper.setCtx(channelHandlerContext);
        wrapper.setRequest(request);
        nettyProcessor.processor(wrapper);
    }
}
