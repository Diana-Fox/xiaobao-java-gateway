package com.xiaobao.core.context;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 *把俩对方放一起
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest request;
    private ChannelHandlerContext ctx;
}
