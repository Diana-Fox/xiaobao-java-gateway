package com.xiaobao.core.request;

import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.Request;

/**
 * 网络请求的接口
 */
public interface IGatewayRequest {
    /**
     * 添加请求头信息
     *
     * @param name
     * @param value
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头信息
     *
     * @param name
     * @param value
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加或者替换Cookie
     *
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 设置超时时间
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 修改域名
     *
     * @param modifyHost
     */
    void setModifyHost(String modifyHost);

    /**
     * 设置路径
     *
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取路径
     *
     * @return
     */
    String getModifyPath();

    /**
     * Get请求参数
     *
     * @param name
     * @param value
     */
    void addQueryParam(String name, String value);

    /**
     * Post请求参数
     *
     * @param name
     * @param value
     */
    void addFormParam(String name, String value);

    /**
     * 获取最终的请求路径
     *
     * @return
     */
    String getFinalUrl();

    /**
     * 构建最终的请求对象
     *
     * @return
     */
    Request build();

}
