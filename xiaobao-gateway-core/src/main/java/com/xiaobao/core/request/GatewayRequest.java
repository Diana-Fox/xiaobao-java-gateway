package com.xiaobao.core.request;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.xiaobao.common.constants.BasicConst;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import java.nio.charset.Charset;
import java.util.*;

public class GatewayRequest implements IGatewayRequest {
    /**
     * 服务Id
     */
    @Getter
    private final String uniquedId;
    /**
     * 开始时间
     */
    @Getter
    private final long beginTime;
    /**
     * 字符集
     */
    @Getter
    private final Charset charset;
    /**
     * 客户端IP，控制黑白名单
     */
    @Getter
    private final String clientIp;

    /**
     * 请求地址 ip:port
     */
    @Getter
    private final String host;
    /**
     * 请求路径
     */
    @Getter
    private final String path;
    /**
     * 统一资源定位符
     */
    @Getter
    private final String uri;

    /**
     * 请求方式 GET POST
     */
    @Getter
    private final HttpMethod method;
    /**
     * 请求格式
     */
    @Getter
    private final String contentType;
    /**
     * 请求头信息
     */
    @Getter
    private final HttpHeaders heasers;
    /**
     * 参数解析器
     */
    @Getter
    private final QueryStringDecoder queryStringDecoder;
    /**
     * 该对象可以获取到所有与HTTP协议相关的信息
     */
    @Getter
    private final FullHttpRequest fullHttpRequest;
    /**
     * 请求体
     */
    @Getter
    private String body;

    /**
     * 请求cookie
     */
    private Map<String, Cookie> cookieMap;
    /**
     * post请求定义的参数
     */


    private Map<String, List<String>> postParameters;

    /*可修改的请求变量*/
    /**
     * 可以修改的Scheme默认是http://
     */
    private String modifyScheme;
    private String modifyHost;
    private String modifyPath;
    /**
     * 构建下游的http请求
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniquedId, Charset charset, String clientIp, String host,
                          String uri, HttpMethod method, String contentType, HttpHeaders heasers
            , FullHttpRequest fullHttpRequest) {
        this.uniquedId = uniquedId;
        this.beginTime = System.currentTimeMillis();//当前时间
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.uri = uri;
        this.method = method;
        this.contentType = contentType;
        this.heasers = heasers;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.path = queryStringDecoder.path();//设置地址
        this.fullHttpRequest = fullHttpRequest;
        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getMethod().name());
        this.requestBuilder.setHeaders(getHeasers());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());
        ByteBuf content = fullHttpRequest.content();
        if (Objects.nonNull(content)) {
            this.requestBuilder.setBody(content.nioBuffer());
        }
    }

    /**
     * 获取请求体
     *
     * @return
     */
    public String getBody() {
        if (StringUtils.isEmpty(body)) {
            //获取一下，转成相应的格式
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    //将http中的cookie转换成netty的cookie
    public Cookie getCookie(String name) {
        if (cookieMap == null) {
            //去加载cookie
            cookieMap = new HashMap<>();
            String cookieStr = getHeasers().get(HttpHeaderNames.COOKIE);
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for (Cookie cookie : cookies) {
                cookieMap.put(name, cookie);
            }
        }
        return cookieMap.get(name);
    }

    /**
     * Get请求获取指定名称的参数
     *
     * @param name
     * @return
     */
    public List<String> getQueryParametersMutiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    /**
     * Post请求获取指定名称的参数
     *
     * @param name
     * @return
     */
    public List<String> getPostParametersMutiple(String name) {
        String bd = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder decoder = new QueryStringDecoder(body, false);
                postParameters = queryStringDecoder.parameters();

            }
            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            return Lists.newArrayList(JsonPath.read(body, name).toString());
        }
        return null;
    }

    private boolean isFormPost() {
        return HttpMethod.POST.equals(method) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    private boolean isJsonPost() {
        return HttpMethod.POST.equals(method) &&
                contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    @Override
    public void setModifyHost(String modifyHost) {
        this.modifyScheme = modifyHost;
    }

    @Override
    public void setModifyPath(String modifyPath) {
        this.modifyPath = modifyPath;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addFormParam(String name, String value) {
        requestBuilder.addFormParam(name, value);
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }
}
