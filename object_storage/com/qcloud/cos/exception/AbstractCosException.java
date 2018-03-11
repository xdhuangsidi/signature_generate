package com.qcloud.cos.exception;

import org.json.JSONObject;

public abstract class AbstractCosException extends Exception {
    private static final long serialVersionUID = 7547532865194837136L;
    private CosExceptionType type;

    public AbstractCosException(CosExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    public CosExceptionType getType() {
        return this.type;
    }

    public String toString() {
        JSONObject responseObj = new JSONObject();
        responseObj.put("code", this.type.getErrorCode());
        responseObj.put("message", getMessage());
        return responseObj.toString();
    }
}
