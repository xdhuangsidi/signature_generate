package com.qcloud.cos;

import org.apache.log4j.Priority;

public class ClientConfig {
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = -1;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_MAX_CONNECTIONS_COUNT = 100;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_SIGN_EXPIRED = 300;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final String DEFAULT_USER_AGENT = "cos-java-sdk-v4.2";
    private static final String DOWN_COS_ENDPOINT_DOMAIN = "cosgz.myqcloud.com";
    private static final String DOWN_COS_ENDPOINT_PREFIX = "http://";
    private static final String UPLOAD_COS_ENDPOINT_DOMAIN = "gz.file.myqcloud.com";
    private static final String UPLOAD_COS_ENDPOINT_PREFIX = "http://";
    private static final String UPLOAD_COS_ENDPOINT_SUFFIX = "/files/v2";
    private int connectionRequestTimeout = -1;
    private int connectionTimeout = Priority.WARN_INT;
    private String downCosEndPointDomain = DOWN_COS_ENDPOINT_DOMAIN;
    private String downCosEndPointPrefix = "http://";
    private String httpProxyIp = null;
    private int httpProxyPort = 0;
    private int maxConnectionsCount = 100;
    private int maxFailedRetry = 3;
    private int signExpired = 300;
    private int socketTimeout = Priority.WARN_INT;
    private String uploadCosEndPointDomain = UPLOAD_COS_ENDPOINT_DOMAIN;
    private String uploadCosEndPointPrefix = "http://";
    private String uploadCosEndPointSuffix = UPLOAD_COS_ENDPOINT_SUFFIX;
    private String userAgent = DEFAULT_USER_AGENT;

    public int getMaxFailedRetry() {
        return this.maxFailedRetry;
    }

    public void setMaxFailedRetry(int maxFailedRetry) {
        this.maxFailedRetry = maxFailedRetry;
    }

    public int getSignExpired() {
        return this.signExpired;
    }

    public void setSignExpired(int signExpired) {
        this.signExpired = signExpired;
    }

    public int getConnectionRequestTimeout() {
        return this.connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return this.socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnectionsCount() {
        return this.maxConnectionsCount;
    }

    public void setMaxConnectionsCount(int maxConnectionsCount) {
        this.maxConnectionsCount = maxConnectionsCount;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUploadCosEndPointDomain() {
        return this.uploadCosEndPointDomain;
    }

    public void setUploadCosEndPointDomain(String cosEndpoint) {
        this.uploadCosEndPointDomain = cosEndpoint;
    }

    public String getDownCosEndPointDomain() {
        return this.downCosEndPointDomain;
    }

    public void setDownCosEndPointDomain(String downCosEndPoint) {
        this.downCosEndPointDomain = downCosEndPoint;
    }

    public String getUploadCosEndPointPrefix() {
        return this.uploadCosEndPointPrefix;
    }

    public void setUploadCosEndPointPrefix(String uploadCosEndPointPrefix) {
        this.uploadCosEndPointPrefix = uploadCosEndPointPrefix;
    }

    public String getUploadCosEndPointSuffix() {
        return this.uploadCosEndPointSuffix;
    }

    public void setUploadCosEndPointSuffix(String uploadCosEndPointSuffix) {
        this.uploadCosEndPointSuffix = uploadCosEndPointSuffix;
    }

    public String getDownCosEndPointPrefix() {
        return this.downCosEndPointPrefix;
    }

    public void setDownCosEndPointPrefix(String downCosEndPointPrefix) {
        this.downCosEndPointPrefix = downCosEndPointPrefix;
    }

    public void setRegion(String region) {
        this.uploadCosEndPointDomain = region + ".file.myqcloud.com";
        this.downCosEndPointDomain = "cos" + region + ".myqcloud.com";
    }

    public String getHttpProxyIp() {
        return this.httpProxyIp;
    }

    public void setHttpProxyIp(String httpProxyIp) {
        this.httpProxyIp = httpProxyIp;
    }

    public int getHttpProxyPort() {
        return this.httpProxyPort;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }
}
