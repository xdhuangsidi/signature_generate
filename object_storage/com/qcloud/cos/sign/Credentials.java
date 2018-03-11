package com.qcloud.cos.sign;

public class Credentials {
    private final long appId;
    private final String secretId;
    private final String secretKey;

    public Credentials(long appId, String secretId, String secretKey) {
        this.appId = appId;
        this.secretId = secretId;
        this.secretKey = secretKey;
    }

    public long getAppId() {
        return this.appId;
    }

    public String getSecretId() {
        return this.secretId;
    }

    public String getSecretKey() {
        return this.secretKey;
    }
}
