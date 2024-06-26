package com.xiaobao.core.helper;

import com.xiaobao.common.config.DynamicConfigMannager;
import com.xiaobao.common.config.ServiceDefinition;
import com.xiaobao.common.constants.BasicConst;
import com.xiaobao.common.constants.GatewayConst;
import com.xiaobao.common.constants.GatewayProtocol;
import com.xiaobao.common.config.Rule;
import com.xiaobao.common.enums.ResponseCode;
import com.xiaobao.common.exception.ResponseException;
import com.xiaobao.core.context.GatewayContext;
import com.xiaobao.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class RequestHelper {

    public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {

        //	构建请求对象GatewayRequest
        GatewayRequest gateWayRequest = doRequest(request, ctx);
        //根据请求对象里的uniqueId获取资源服务信息（也就是服务定义信息）
        ServiceDefinition serviceDefinition = DynamicConfigMannager.getInstance().getServiceDefinition(gateWayRequest.getUniquedId());

        //根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
        Rule rule = getRule(gateWayRequest, serviceDefinition.getServiceId());

        //	构建我们而定GateWayContext对象
        GatewayContext gatewayContext = new GatewayContext(
//				GatewayProtocol.HTTP,// TODO 暂时写死
                serviceDefinition.getProtocol(),
                ctx,
                HttpUtil.isKeepAlive(request),
                gateWayRequest, rule);

        //这里要做负载均衡了

        //暂时：后续服务发现做完，这里都要改成动态的
        gatewayContext.getRequest().setModifyHost("127.0.0.1:8080");

        return gatewayContext;
    }

    /**
     * 获取规则
     *
     * @param gateWayRequest
     * @param serviceId
     * @return
     */
    private static Rule getRule(GatewayRequest gateWayRequest, String serviceId) {
        String key = serviceId + "." + gateWayRequest.getPath();
        Rule rule = DynamicConfigMannager.getInstance().getRuleByPath(key);
        if (rule != null) {
            return rule;
        }
        return DynamicConfigMannager.getInstance().
                getRuleByServiceId(serviceId).stream().
                filter(f -> gateWayRequest.getPath().startsWith(rule.getPrefix())).
                findAny().orElseThrow(() -> new ResponseException(ResponseCode.PATH_NO_MATCHED));
    }

    /**
     * 构建Request请求对象
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

        HttpHeaders headers = fullHttpRequest.headers();
        //	从header头获取必须要传入的关键属性 uniqueId
        String uniqueId = headers.get(GatewayConst.UNIQUE_ID);

        String host = headers.get(HttpHeaderNames.HOST);
        // 调用方法
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        // 数据类型
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        GatewayRequest gatewayRequest = new GatewayRequest(uniqueId,
                charset,
                clientIp,
                host,
                uri,
                method,
                contentType,
                headers,
                fullHttpRequest);

        return gatewayRequest;
    }

    /**
     * 获取客户端ip
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }


}
