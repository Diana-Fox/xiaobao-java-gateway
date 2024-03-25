package com.xiaobao.core.context;

import com.xiaobao.core.request.GatewayRequest;
import com.xiaobao.core.response.GatewayResponse;
import io.netty.channel.ChannelHandlerContext;
import com.xiaobao.common.rule.Rule;
import io.netty.util.ReferenceCountUtil;
import lombok.Setter;

public class GatewayContext extends BasicContext {
    /**
     * 请求
     */
    private GatewayRequest request;
    /**
     * 响应
     */
    @Setter
    private GatewayResponse response;
    /**
     * 规则
     */
    private Rule rule;

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                          GatewayRequest request, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.rule = rule;
        this.request = request;
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }

    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }
    public String getUniqueId(){
        return request.getUniquedId();
    }

    /**
     *重写父类释放资源方法，释放资源
     */
    public void releaseRequest(){
        if (requestReleased.compareAndSet(false,true)){
            //释放资源
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }
}
