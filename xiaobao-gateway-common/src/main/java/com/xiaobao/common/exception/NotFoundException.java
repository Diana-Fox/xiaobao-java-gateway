package com.xiaobao.common.exception;

import com.xiaobao.common.enums.ResponseCode;

public class NotFoundException extends BaseException{
    public NotFoundException(ResponseCode code){
        super(code.getMessage(),code);
    }
}
