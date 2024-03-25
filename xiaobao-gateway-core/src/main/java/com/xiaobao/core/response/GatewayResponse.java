package com.xiaobao.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xiaobao.common.enums.ResponseCode;
import com.xiaobao.common.util.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

@Data
public class GatewayResponse {
    //响应头
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();
    //额外的响应结果
    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();
    //返回响应的结果
    private String content;
    //返回响应的状态码
    private HttpResponseStatus httpResponseStatus;
    //异步的返回对象
    private Response futureResponse;

    public GatewayResponse() {
    }

    /**
     * 设置响应头信息
     */

    public void putHeader(CharSequence key, CharSequence val) {
        responseHeaders.add(key, val);
    }

    /**
     * 构建网关响应对象
     * @param futureResponse
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }

    /**
     * 返回一个json类型的响应信息，失败的时候使用
     * @param code
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
        //-------------------封装要返回的内容------------------------------
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());
        //------------------封装http响应----------------------------
        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(code.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }

    /**
     * 返回一个json类型的响应信息
     * @param data
     * @return
     */
    public static GatewayResponse buildGatewayResponse( Object data) {
        //-------------------封装要返回的内容------------------------------
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.put(JSONUtil.MESSAGE,  ResponseCode.SUCCESS.getMessage());
        objectNode.putPOJO(JSONUtil.DATA,data);
        //------------------封装http响应----------------------------
        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus( ResponseCode.SUCCESS.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }
}
