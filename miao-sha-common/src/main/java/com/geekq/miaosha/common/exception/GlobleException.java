package com.geekq.miaosha.common.exception;


import com.geekq.miaosha.common.enums.enums.ResultStatus;

public class GlobleException extends RuntimeException {


    private ResultStatus status;

    public GlobleException(ResultStatus status){
        super();
        this.status = status;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }
}
