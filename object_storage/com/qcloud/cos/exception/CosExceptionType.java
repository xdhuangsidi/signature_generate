package com.qcloud.cos.exception;

public enum CosExceptionType {
    PARAM_EXCEPTION(-1, "param_excepiton"),
    NETWORK_EXCEPITON(-2, "network_excepiton"),
    SERVER_EXCEPTION(-3, "server_exception"),
    UNKNOWN_EXCEPTION(-4, "unknown_exception");
    
    private int errorCode;
    private String exceptionStr;

    private CosExceptionType(int errorCode, String exceptionStr) {
        this.errorCode = errorCode;
        this.exceptionStr = exceptionStr;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getExceptionStr() {
        return this.exceptionStr;
    }
}
