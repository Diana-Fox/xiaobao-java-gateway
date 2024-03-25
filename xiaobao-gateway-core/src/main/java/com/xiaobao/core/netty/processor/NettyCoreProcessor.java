package com.xiaobao.core.netty.processor;

import com.xiaobao.common.enums.ResponseCode;
import com.xiaobao.common.exception.BaseException;
import com.xiaobao.common.exception.ConnectException;
import com.xiaobao.common.exception.ResponseException;
import com.xiaobao.core.config.ConfigLoader;
import com.xiaobao.core.context.GatewayContext;
import com.xiaobao.core.context.HttpRequestWrapper;
import com.xiaobao.core.helper.AsyncHttpHelper;
import com.xiaobao.core.helper.RequestHelper;
import com.xiaobao.core.helper.ResponseHelper;
import com.xiaobao.core.response.GatewayResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NettyCoreProcessor implements NettyProcessor {
    @Override
    public void processor(HttpRequestWrapper wrapper) {
        ChannelHandlerContext ctx = wrapper.getCtx();
        FullHttpRequest request = wrapper.getRequest();
        try {
            //1、创建网关的上下文
            GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);
            //2、路由请求获取信息
            route(gatewayContext);
        } catch (BaseException e) {
            log.error("processor error {},{}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(ctx, request, httpResponse);
        } catch (Throwable t) {
            log.error("processor unkown error:", t);
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx, request, httpResponse);
        }

    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param request
     * @param httpResponse
     */
    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse httpResponse) {
        ctx.writeAndFlush(httpResponse).
                addListener(ChannelFutureListener.CLOSE);//释放资源后关闭channel
        ReferenceCountUtil.release(request);//释放资源
    }

    /**
     * 发送相应的请求出去
     *
     * @param gatewayContext
     */
    private void route(GatewayContext gatewayContext) {
        //构造异步的请求
        Request request = gatewayContext.getRequest().build();
        //发送请求
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        //判断是单异步还是双异步
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if (whenComplete) {
            future.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        } else {
            future.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        }
    }

    /**
     * 处理返回值
     *
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext) {
        //释放资源
        gatewayContext.releaseRequest();
        try {

            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                //调用超时
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    //超时异常
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    //其他异常
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(),
                            url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Throwable t) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", t);
        } finally {
            //改变Context状态
            gatewayContext.written();
            //写回数据
            ResponseHelper.writeResponse(gatewayContext);
        }
    }
}
