package com.xiaobao.core.netty.processor;

import com.xiaobao.core.context.HttpRequestWrapper;

public interface NettyProcessor {
    void processor(HttpRequestWrapper wrapper);
}
