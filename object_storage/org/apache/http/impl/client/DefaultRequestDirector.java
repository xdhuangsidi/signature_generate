package org.apache.http.impl.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

@NotThreadSafe
@Deprecated
public class DefaultRequestDirector implements RequestDirector {
    private final HttpAuthenticator authenticator;
    protected final ClientConnectionManager connManager;
    private int execCount;
    protected final HttpProcessor httpProcessor;
    protected final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final Log log;
    protected ManagedClientConnection managedConn;
    private final int maxRedirects;
    protected final HttpParams params;
    @Deprecated
    protected final AuthenticationHandler proxyAuthHandler;
    protected final AuthState proxyAuthState;
    protected final AuthenticationStrategy proxyAuthStrategy;
    private int redirectCount;
    @Deprecated
    protected final RedirectHandler redirectHandler;
    protected final RedirectStrategy redirectStrategy;
    protected final HttpRequestExecutor requestExec;
    protected final HttpRequestRetryHandler retryHandler;
    protected final ConnectionReuseStrategy reuseStrategy;
    protected final HttpRoutePlanner routePlanner;
    @Deprecated
    protected final AuthenticationHandler targetAuthHandler;
    protected final AuthState targetAuthState;
    protected final AuthenticationStrategy targetAuthStrategy;
    protected final UserTokenHandler userTokenHandler;
    private HttpHost virtualHost;

    @Deprecated
    public DefaultRequestDirector(HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectHandler redirectHandler, AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler, UserTokenHandler userTokenHandler, HttpParams params) {
        this(LogFactory.getLog(DefaultRequestDirector.class), requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, new DefaultRedirectStrategyAdaptor(redirectHandler), new AuthenticationStrategyAdaptor(targetAuthHandler), new AuthenticationStrategyAdaptor(proxyAuthHandler), userTokenHandler, params);
    }

    @Deprecated
    public DefaultRequestDirector(Log log, HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectStrategy redirectStrategy, AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler, UserTokenHandler userTokenHandler, HttpParams params) {
        this(LogFactory.getLog(DefaultRequestDirector.class), requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectStrategy, new AuthenticationStrategyAdaptor(targetAuthHandler), new AuthenticationStrategyAdaptor(proxyAuthHandler), userTokenHandler, params);
    }

    public DefaultRequestDirector(Log log, HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectStrategy redirectStrategy, AuthenticationStrategy targetAuthStrategy, AuthenticationStrategy proxyAuthStrategy, UserTokenHandler userTokenHandler, HttpParams params) {
        Args.notNull(log, "Log");
        Args.notNull(requestExec, "Request executor");
        Args.notNull(conman, "Client connection manager");
        Args.notNull(reustrat, "Connection reuse strategy");
        Args.notNull(kastrat, "Connection keep alive strategy");
        Args.notNull(rouplan, "Route planner");
        Args.notNull(httpProcessor, "HTTP protocol processor");
        Args.notNull(retryHandler, "HTTP request retry handler");
        Args.notNull(redirectStrategy, "Redirect strategy");
        Args.notNull(targetAuthStrategy, "Target authentication strategy");
        Args.notNull(proxyAuthStrategy, "Proxy authentication strategy");
        Args.notNull(userTokenHandler, "User token handler");
        Args.notNull(params, "HTTP parameters");
        this.log = log;
        this.authenticator = new HttpAuthenticator(log);
        this.requestExec = requestExec;
        this.connManager = conman;
        this.reuseStrategy = reustrat;
        this.keepAliveStrategy = kastrat;
        this.routePlanner = rouplan;
        this.httpProcessor = httpProcessor;
        this.retryHandler = retryHandler;
        this.redirectStrategy = redirectStrategy;
        this.targetAuthStrategy = targetAuthStrategy;
        this.proxyAuthStrategy = proxyAuthStrategy;
        this.userTokenHandler = userTokenHandler;
        this.params = params;
        if (redirectStrategy instanceof DefaultRedirectStrategyAdaptor) {
            this.redirectHandler = ((DefaultRedirectStrategyAdaptor) redirectStrategy).getHandler();
        } else {
            this.redirectHandler = null;
        }
        if (targetAuthStrategy instanceof AuthenticationStrategyAdaptor) {
            this.targetAuthHandler = ((AuthenticationStrategyAdaptor) targetAuthStrategy).getHandler();
        } else {
            this.targetAuthHandler = null;
        }
        if (proxyAuthStrategy instanceof AuthenticationStrategyAdaptor) {
            this.proxyAuthHandler = ((AuthenticationStrategyAdaptor) proxyAuthStrategy).getHandler();
        } else {
            this.proxyAuthHandler = null;
        }
        this.managedConn = null;
        this.execCount = 0;
        this.redirectCount = 0;
        this.targetAuthState = new AuthState();
        this.proxyAuthState = new AuthState();
        this.maxRedirects = this.params.getIntParameter(ClientPNames.MAX_REDIRECTS, 100);
    }

    private RequestWrapper wrapRequest(HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) request);
        }
        return new RequestWrapper(request);
    }

    protected void rewriteRequestURI(RequestWrapper request, HttpRoute route) throws ProtocolException {
        try {
            URI uri = request.getURI();
            if (route.getProxyHost() == null || route.isTunnelled()) {
                if (uri.isAbsolute()) {
                    uri = URIUtils.rewriteURI(uri, null, true);
                } else {
                    uri = URIUtils.rewriteURI(uri);
                }
            } else if (uri.isAbsolute()) {
                uri = URIUtils.rewriteURI(uri);
            } else {
                uri = URIUtils.rewriteURI(uri, route.getTargetHost(), true);
            }
            request.setURI(uri);
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid URI: " + request.getRequestLine().getUri(), ex);
        }
    }

    public HttpResponse execute(HttpHost targetHost, HttpRequest request, HttpContext context) throws HttpException, IOException {
        context.setAttribute("http.auth.target-scope", this.targetAuthState);
        context.setAttribute("http.auth.proxy-scope", this.proxyAuthState);
        HttpHost target = targetHost;
        HttpRequest orig = request;
        RequestWrapper origWrapper = wrapRequest(orig);
        origWrapper.setParams(this.params);
        HttpRoute origRoute = determineRoute(target, origWrapper, context);
        this.virtualHost = (HttpHost) origWrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);
        if (this.virtualHost != null && this.virtualHost.getPort() == -1) {
            int port = (target != null ? target : origRoute.getTargetHost()).getPort();
            if (port != -1) {
                this.virtualHost = new HttpHost(this.virtualHost.getHostName(), port, this.virtualHost.getSchemeName());
            }
        }
        RoutedRequest routedRequest = new RoutedRequest(origWrapper, origRoute);
        boolean reuse = false;
        boolean done = false;
        HttpResponse response = null;
        while (!done) {
            try {
                RoutedRequest roureq;
                RequestWrapper wrapper = roureq.getRequest();
                HttpRoute route = roureq.getRoute();
                Object userToken = context.getAttribute("http.user-token");
                if (this.managedConn == null) {
                    ClientConnectionRequest connRequest = this.connManager.requestConnection(route, userToken);
                    if (orig instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) orig).setConnectionRequest(connRequest);
                    }
                    this.managedConn = connRequest.getConnection(HttpClientParams.getConnectionManagerTimeout(this.params), TimeUnit.MILLISECONDS);
                    if (HttpConnectionParams.isStaleCheckingEnabled(this.params) && this.managedConn.isOpen()) {
                        this.log.debug("Stale connection check");
                        if (this.managedConn.isStale()) {
                            this.log.debug("Stale connection detected");
                            this.managedConn.close();
                        }
                    }
                }
                if (orig instanceof AbortableHttpRequest) {
                    ((AbortableHttpRequest) orig).setReleaseTrigger(this.managedConn);
                }
                try {
                    tryConnect(roureq, context);
                    String userinfo = wrapper.getURI().getUserInfo();
                    if (userinfo != null) {
                        this.targetAuthState.update(new BasicScheme(), new UsernamePasswordCredentials(userinfo));
                    }
                    if (this.virtualHost != null) {
                        target = this.virtualHost;
                    } else {
                        URI requestURI = wrapper.getURI();
                        if (requestURI.isAbsolute()) {
                            target = URIUtils.extractHost(requestURI);
                        }
                    }
                    if (target == null) {
                        target = route.getTargetHost();
                    }
                    wrapper.resetHeaders();
                    rewriteRequestURI(wrapper, route);
                    context.setAttribute("http.target_host", target);
                    context.setAttribute("http.route", route);
                    context.setAttribute("http.connection", this.managedConn);
                    this.requestExec.preProcess(wrapper, this.httpProcessor, context);
                    response = tryExecute(roureq, context);
                    if (response != null) {
                        response.setParams(this.params);
                        this.requestExec.postProcess(response, this.httpProcessor, context);
                        reuse = this.reuseStrategy.keepAlive(response, context);
                        if (reuse) {
                            long duration = this.keepAliveStrategy.getKeepAliveDuration(response, context);
                            if (this.log.isDebugEnabled()) {
                                String s;
                                if (duration > 0) {
                                    s = "for " + duration + " " + TimeUnit.MILLISECONDS;
                                } else {
                                    s = "indefinitely";
                                }
                                this.log.debug("Connection can be kept alive " + s);
                            }
                            this.managedConn.setIdleDuration(duration, TimeUnit.MILLISECONDS);
                        }
                        RoutedRequest followup = handleResponse(roureq, response, context);
                        if (followup == null) {
                            done = true;
                        } else {
                            if (reuse) {
                                EntityUtils.consume(response.getEntity());
                                this.managedConn.markReusable();
                            } else {
                                this.managedConn.close();
                                if (this.proxyAuthState.getState().compareTo(AuthProtocolState.CHALLENGED) > 0 && this.proxyAuthState.getAuthScheme() != null && this.proxyAuthState.getAuthScheme().isConnectionBased()) {
                                    this.log.debug("Resetting proxy auth state");
                                    this.proxyAuthState.reset();
                                }
                                if (this.targetAuthState.getState().compareTo(AuthProtocolState.CHALLENGED) > 0 && this.targetAuthState.getAuthScheme() != null && this.targetAuthState.getAuthScheme().isConnectionBased()) {
                                    this.log.debug("Resetting target auth state");
                                    this.targetAuthState.reset();
                                }
                            }
                            if (!followup.getRoute().equals(roureq.getRoute())) {
                                releaseConnection();
                            }
                            roureq = followup;
                        }
                        if (this.managedConn != null) {
                            if (userToken == null) {
                                userToken = this.userTokenHandler.getUserToken(context);
                                context.setAttribute("http.user-token", userToken);
                            }
                            if (userToken != null) {
                                this.managedConn.setState(userToken);
                            }
                        }
                    }
                } catch (TunnelRefusedException ex) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug(ex.getMessage());
                    }
                    response = ex.getResponse();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            } catch (ConnectionShutdownException ex2) {
                InterruptedIOException ioex = new InterruptedIOException("Connection has been shut down");
                ioex.initCause(ex2);
                throw ioex;
            } catch (HttpException ex3) {
                abortConnection();
                throw ex3;
            } catch (IOException ex4) {
                abortConnection();
                throw ex4;
            } catch (RuntimeException ex5) {
                abortConnection();
                throw ex5;
            }
        }
        if (response == null || response.getEntity() == null || !response.getEntity().isStreaming()) {
            if (reuse) {
                this.managedConn.markReusable();
            }
            releaseConnection();
        } else {
            response.setEntity(new BasicManagedEntity(response.getEntity(), this.managedConn, reuse));
        }
        return response;
    }

    private void tryConnect(RoutedRequest req, HttpContext context) throws HttpException, IOException {
        HttpRoute route = req.getRoute();
        HttpRequest wrapper = req.getRequest();
        int connectCount = 0;
        while (true) {
            context.setAttribute("http.request", wrapper);
            connectCount++;
            try {
                break;
            } catch (IOException ex) {
                try {
                    this.managedConn.close();
                } catch (IOException e) {
                }
                if (!this.retryHandler.retryRequest(ex, connectCount, context)) {
                    throw ex;
                } else if (this.log.isInfoEnabled()) {
                    this.log.info("I/O exception (" + ex.getClass().getName() + ") caught when connecting to " + route + ": " + ex.getMessage());
                    if (this.log.isDebugEnabled()) {
                        this.log.debug(ex.getMessage(), ex);
                    }
                    this.log.info("Retrying connect to " + route);
                }
            }
        }
        if (this.managedConn.isOpen()) {
            this.managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(this.params));
        } else {
            this.managedConn.open(route, context, this.params);
        }
        establishRoute(route, context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.apache.http.HttpResponse tryExecute(org.apache.http.impl.client.RoutedRequest r10, org.apache.http.protocol.HttpContext r11) throws org.apache.http.HttpException, java.io.IOException {
        /*
        r9 = this;
        r5 = r10.getRequest();
        r3 = r10.getRoute();
        r1 = 0;
        r2 = 0;
    L_0x000a:
        r6 = r9.execCount;
        r6 = r6 + 1;
        r9.execCount = r6;
        r5.incrementExecCount();
        r6 = r5.isRepeatable();
        if (r6 != 0) goto L_0x0032;
    L_0x0019:
        r6 = r9.log;
        r7 = "Cannot retry non-repeatable request";
        r6.debug(r7);
        if (r2 == 0) goto L_0x002a;
    L_0x0022:
        r6 = new org.apache.http.client.NonRepeatableRequestException;
        r7 = "Cannot retry request with a non-repeatable request entity.  The cause lists the reason the original request failed.";
        r6.<init>(r7, r2);
        throw r6;
    L_0x002a:
        r6 = new org.apache.http.client.NonRepeatableRequestException;
        r7 = "Cannot retry request with a non-repeatable request entity.";
        r6.<init>(r7);
        throw r6;
    L_0x0032:
        r6 = r9.managedConn;	 Catch:{ IOException -> 0x0087 }
        r6 = r6.isOpen();	 Catch:{ IOException -> 0x0087 }
        if (r6 != 0) goto L_0x004e;
    L_0x003a:
        r6 = r3.isTunnelled();	 Catch:{ IOException -> 0x0087 }
        if (r6 != 0) goto L_0x007f;
    L_0x0040:
        r6 = r9.log;	 Catch:{ IOException -> 0x0087 }
        r7 = "Reopening the direct connection.";
        r6.debug(r7);	 Catch:{ IOException -> 0x0087 }
        r6 = r9.managedConn;	 Catch:{ IOException -> 0x0087 }
        r7 = r9.params;	 Catch:{ IOException -> 0x0087 }
        r6.open(r3, r11, r7);	 Catch:{ IOException -> 0x0087 }
    L_0x004e:
        r6 = r9.log;	 Catch:{ IOException -> 0x0087 }
        r6 = r6.isDebugEnabled();	 Catch:{ IOException -> 0x0087 }
        if (r6 == 0) goto L_0x0076;
    L_0x0056:
        r6 = r9.log;	 Catch:{ IOException -> 0x0087 }
        r7 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0087 }
        r7.<init>();	 Catch:{ IOException -> 0x0087 }
        r8 = "Attempt ";
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0087 }
        r8 = r9.execCount;	 Catch:{ IOException -> 0x0087 }
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0087 }
        r8 = " to execute request";
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0087 }
        r7 = r7.toString();	 Catch:{ IOException -> 0x0087 }
        r6.debug(r7);	 Catch:{ IOException -> 0x0087 }
    L_0x0076:
        r6 = r9.requestExec;	 Catch:{ IOException -> 0x0087 }
        r7 = r9.managedConn;	 Catch:{ IOException -> 0x0087 }
        r1 = r6.execute(r5, r7, r11);	 Catch:{ IOException -> 0x0087 }
    L_0x007e:
        return r1;
    L_0x007f:
        r6 = r9.log;	 Catch:{ IOException -> 0x0087 }
        r7 = "Proxied connection. Need to start over.";
        r6.debug(r7);	 Catch:{ IOException -> 0x0087 }
        goto L_0x007e;
    L_0x0087:
        r0 = move-exception;
        r6 = r9.log;
        r7 = "Closing the connection.";
        r6.debug(r7);
        r6 = r9.managedConn;	 Catch:{ IOException -> 0x0141 }
        r6.close();	 Catch:{ IOException -> 0x0141 }
    L_0x0094:
        r6 = r9.retryHandler;
        r7 = r5.getExecCount();
        r6 = r6.retryRequest(r0, r7, r11);
        if (r6 == 0) goto L_0x0114;
    L_0x00a0:
        r6 = r9.log;
        r6 = r6.isInfoEnabled();
        if (r6 == 0) goto L_0x00e0;
    L_0x00a8:
        r6 = r9.log;
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "I/O exception (";
        r7 = r7.append(r8);
        r8 = r0.getClass();
        r8 = r8.getName();
        r7 = r7.append(r8);
        r8 = ") caught when processing request to ";
        r7 = r7.append(r8);
        r7 = r7.append(r3);
        r8 = ": ";
        r7 = r7.append(r8);
        r8 = r0.getMessage();
        r7 = r7.append(r8);
        r7 = r7.toString();
        r6.info(r7);
    L_0x00e0:
        r6 = r9.log;
        r6 = r6.isDebugEnabled();
        if (r6 == 0) goto L_0x00f1;
    L_0x00e8:
        r6 = r9.log;
        r7 = r0.getMessage();
        r6.debug(r7, r0);
    L_0x00f1:
        r6 = r9.log;
        r6 = r6.isInfoEnabled();
        if (r6 == 0) goto L_0x0111;
    L_0x00f9:
        r6 = r9.log;
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "Retrying request to ";
        r7 = r7.append(r8);
        r7 = r7.append(r3);
        r7 = r7.toString();
        r6.info(r7);
    L_0x0111:
        r2 = r0;
        goto L_0x000a;
    L_0x0114:
        r6 = r0 instanceof org.apache.http.NoHttpResponseException;
        if (r6 == 0) goto L_0x0140;
    L_0x0118:
        r4 = new org.apache.http.NoHttpResponseException;
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = r3.getTargetHost();
        r7 = r7.toHostString();
        r6 = r6.append(r7);
        r7 = " failed to respond";
        r6 = r6.append(r7);
        r6 = r6.toString();
        r4.<init>(r6);
        r6 = r0.getStackTrace();
        r4.setStackTrace(r6);
        throw r4;
    L_0x0140:
        throw r0;
    L_0x0141:
        r6 = move-exception;
        goto L_0x0094;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.client.DefaultRequestDirector.tryExecute(org.apache.http.impl.client.RoutedRequest, org.apache.http.protocol.HttpContext):org.apache.http.HttpResponse");
    }

    protected void releaseConnection() {
        try {
            this.managedConn.releaseConnection();
        } catch (IOException ignored) {
            this.log.debug("IOException releasing connection", ignored);
        }
        this.managedConn = null;
    }

    protected HttpRoute determineRoute(HttpHost targetHost, HttpRequest request, HttpContext context) throws HttpException {
        HttpRoutePlanner httpRoutePlanner = this.routePlanner;
        if (targetHost == null) {
            targetHost = (HttpHost) request.getParams().getParameter(ClientPNames.DEFAULT_HOST);
        }
        return httpRoutePlanner.determineRoute(targetHost, request, context);
    }

    protected void establishRoute(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpRouteDirector rowdy = new BasicRouteDirector();
        int step;
        do {
            HttpRoute fact = this.managedConn.getRoute();
            step = rowdy.nextStep(route, fact);
            boolean secure;
            switch (step) {
                case -1:
                    throw new HttpException("Unable to establish route: planned = " + route + "; current = " + fact);
                case 0:
                    break;
                case 1:
                case 2:
                    this.managedConn.open(route, context, this.params);
                    continue;
                case 3:
                    secure = createTunnelToTarget(route, context);
                    this.log.debug("Tunnel to target created.");
                    this.managedConn.tunnelTarget(secure, this.params);
                    continue;
                case 4:
                    int hop = fact.getHopCount() - 1;
                    secure = createTunnelToProxy(route, hop, context);
                    this.log.debug("Tunnel to proxy created.");
                    this.managedConn.tunnelProxy(route.getHopTarget(hop), secure, this.params);
                    continue;
                case 5:
                    this.managedConn.layerProtocol(context, this.params);
                    continue;
                default:
                    throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
            }
        } while (step > 0);
    }

    protected boolean createTunnelToTarget(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpHost proxy = route.getProxyHost();
        HttpHost target = route.getTargetHost();
        while (true) {
            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, context, this.params);
            }
            HttpRequest connect = createConnectRequest(route, context);
            connect.setParams(this.params);
            context.setAttribute("http.target_host", target);
            context.setAttribute("http.route", route);
            context.setAttribute(ExecutionContext.HTTP_PROXY_HOST, proxy);
            context.setAttribute("http.connection", this.managedConn);
            context.setAttribute("http.request", connect);
            this.requestExec.preProcess(connect, this.httpProcessor, context);
            HttpResponse response = this.requestExec.execute(connect, this.managedConn, context);
            response.setParams(this.params);
            this.requestExec.postProcess(response, this.httpProcessor, context);
            if (response.getStatusLine().getStatusCode() < HttpStatus.SC_OK) {
                throw new HttpException("Unexpected response to CONNECT request: " + response.getStatusLine());
            } else if (HttpClientParams.isAuthenticating(this.params)) {
                if (this.authenticator.isAuthenticationRequested(proxy, response, this.proxyAuthStrategy, this.proxyAuthState, context) && this.authenticator.authenticate(proxy, response, this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    if (this.reuseStrategy.keepAlive(response, context)) {
                        this.log.debug("Connection kept alive");
                        EntityUtils.consume(response.getEntity());
                    } else {
                        this.managedConn.close();
                    }
                }
            }
        }
        if (response.getStatusLine().getStatusCode() > 299) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntity(entity));
            }
            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + response.getStatusLine(), response);
        }
        this.managedConn.markReusable();
        return false;
    }

    protected boolean createTunnelToProxy(HttpRoute route, int hop, HttpContext context) throws HttpException, IOException {
        throw new HttpException("Proxy chains are not supported.");
    }

    protected HttpRequest createConnectRequest(HttpRoute route, HttpContext context) {
        HttpHost target = route.getTargetHost();
        String host = target.getHostName();
        int port = target.getPort();
        if (port < 0) {
            port = this.connManager.getSchemeRegistry().getScheme(target.getSchemeName()).getDefaultPort();
        }
        StringBuilder buffer = new StringBuilder(host.length() + 6);
        buffer.append(host);
        buffer.append(':');
        buffer.append(Integer.toString(port));
        return new BasicHttpRequest("CONNECT", buffer.toString(), HttpProtocolParams.getVersion(this.params));
    }

    protected RoutedRequest handleResponse(RoutedRequest roureq, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpRoute route = roureq.getRoute();
        RequestWrapper request = roureq.getRequest();
        HttpParams params = request.getParams();
        if (HttpClientParams.isAuthenticating(params)) {
            HttpHost target = (HttpHost) context.getAttribute("http.target_host");
            if (target == null) {
                target = route.getTargetHost();
            }
            if (target.getPort() < 0) {
                target = new HttpHost(target.getHostName(), this.connManager.getSchemeRegistry().getScheme(target).getDefaultPort(), target.getSchemeName());
            }
            boolean targetAuthRequested = this.authenticator.isAuthenticationRequested(target, response, this.targetAuthStrategy, this.targetAuthState, context);
            HttpHost proxy = route.getProxyHost();
            if (proxy == null) {
                proxy = route.getTargetHost();
            }
            boolean proxyAuthRequested = this.authenticator.isAuthenticationRequested(proxy, response, this.proxyAuthStrategy, this.proxyAuthState, context);
            if (targetAuthRequested) {
                if (this.authenticator.authenticate(target, response, this.targetAuthStrategy, this.targetAuthState, context)) {
                    return roureq;
                }
            }
            if (proxyAuthRequested) {
                if (this.authenticator.authenticate(proxy, response, this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    return roureq;
                }
            }
        }
        if (!HttpClientParams.isRedirecting(params) || !this.redirectStrategy.isRedirected(request, response, context)) {
            return null;
        }
        if (this.redirectCount >= this.maxRedirects) {
            throw new RedirectException("Maximum redirects (" + this.maxRedirects + ") exceeded");
        }
        this.redirectCount++;
        this.virtualHost = null;
        HttpUriRequest redirect = this.redirectStrategy.getRedirect(request, response, context);
        redirect.setHeaders(request.getOriginal().getAllHeaders());
        URI uri = redirect.getURI();
        HttpHost newTarget = URIUtils.extractHost(uri);
        if (newTarget == null) {
            throw new ProtocolException("Redirect URI does not specify a valid host name: " + uri);
        }
        if (!route.getTargetHost().equals(newTarget)) {
            this.log.debug("Resetting target auth state");
            this.targetAuthState.reset();
            AuthScheme authScheme = this.proxyAuthState.getAuthScheme();
            if (authScheme != null && authScheme.isConnectionBased()) {
                this.log.debug("Resetting proxy auth state");
                this.proxyAuthState.reset();
            }
        }
        RequestWrapper wrapper = wrapRequest(redirect);
        wrapper.setParams(params);
        HttpRoute newRoute = determineRoute(newTarget, wrapper, context);
        RoutedRequest newRequest = new RoutedRequest(wrapper, newRoute);
        if (this.log.isDebugEnabled()) {
            this.log.debug("Redirecting to '" + uri + "' via " + newRoute);
        }
        return newRequest;
    }

    private void abortConnection() {
        ManagedClientConnection mcc = this.managedConn;
        if (mcc != null) {
            this.managedConn = null;
            try {
                mcc.abortConnection();
            } catch (IOException ex) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(ex.getMessage(), ex);
                }
            }
            try {
                mcc.releaseConnection();
            } catch (IOException ignored) {
                this.log.debug("Error releasing connection", ignored);
            }
        }
    }
}
