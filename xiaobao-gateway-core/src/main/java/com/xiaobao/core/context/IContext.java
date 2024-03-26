package com.xiaobao.core.context;


import io.netty.channel.ChannelHandlerContext;
import com.xiaobao.common.config.Rule;

import java.util.Map;
import java.util.function.Consumer;

//核心上下文接口
public interface IContext {
    //生命周期状态
    int RUNNING = 0;//请求正在执行
    int WRITTEN = 1;//请求结束，写回reponse,返回结果
    int COMPLETED = 2;//响应成功
    int TERMINATED = -1;//网关请求完毕，彻底结束

    /**
     * 设置上下文状态为正常运行状态
     */
    void running();

    /**
     * 设置上下文状态为标记协会状态
     */
    void written();

    /**
     * 设置上下文状态为标记写回成功
     */
    void completed();

    /**
     * 标记网关结束
     */
    void terminated();

    /**
     * 判断网关的运行状态
     */
    boolean isRunning();

    boolean isWritten();

    boolean isCompleted();

    boolean isTerminated();

    /**
     * 获取请求协议
     */
    String getProtocol();

    /**
     * 获取请求规则
     */
    Rule getRule();

    /**
     * 获取请求对象
     *
     * @return
     */
    Object getRequest();

    /**
     * 获取响应对象
     *
     * @return
     */
    Object getResponse();

    /**
     * 获取异常信息
     */
    Throwable getThrowable();

    /**
     * 获取上下文请求参数
     */
    Object getAttribute(Map<String, Object> key);

    /**
     * 设置请求规则
     */
    void setRule(Rule rule);
//    void setRequest(String request);

    /**
     * 设置返回结果
     */
    void setResponse();

    /**
     * 设置异常信息
     */
    void setThrowable(Throwable throwable);

    /**
     * 设置上下文参数
     * @param key
     * @param object
     */
    void setAttribute(String key,Object object);
    /**
     * 获取netty上下文
     */
    ChannelHandlerContext getNettyCtx();
    /**
     * 是否保持长连接
     */
    boolean isKeepAlive();
    /**
     * 释放资源
     */
    void releaseRequest();
    /**
     * 设置回调函数
     */
    void setCompleted(Consumer<IContext> consumer);
    /**
     * 调用回调函数
     */
    void invokeCompletedCallBack();
}
