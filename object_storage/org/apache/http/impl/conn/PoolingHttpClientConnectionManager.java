package org.apache.http.impl.conn;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpClientConnectionOperator;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.pool.ConnFactory;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@ThreadSafe
public class PoolingHttpClientConnectionManager implements HttpClientConnectionManager, ConnPoolControl<HttpRoute>, Closeable {
    private final ConfigData configData;
    private final HttpClientConnectionOperator connectionOperator;
    private final AtomicBoolean isShutDown;
    private final Log log;
    private final CPool pool;

    static class ConfigData {
        private final Map<HttpHost, ConnectionConfig> connectionConfigMap = new ConcurrentHashMap();
        private volatile ConnectionConfig defaultConnectionConfig;
        private volatile SocketConfig defaultSocketConfig;
        private final Map<HttpHost, SocketConfig> socketConfigMap = new ConcurrentHashMap();

        ConfigData() {
        }

        public SocketConfig getDefaultSocketConfig() {
            return this.defaultSocketConfig;
        }

        public void setDefaultSocketConfig(SocketConfig defaultSocketConfig) {
            this.defaultSocketConfig = defaultSocketConfig;
        }

        public ConnectionConfig getDefaultConnectionConfig() {
            return this.defaultConnectionConfig;
        }

        public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig) {
            this.defaultConnectionConfig = defaultConnectionConfig;
        }

        public SocketConfig getSocketConfig(HttpHost host) {
            return (SocketConfig) this.socketConfigMap.get(host);
        }

        public void setSocketConfig(HttpHost host, SocketConfig socketConfig) {
            this.socketConfigMap.put(host, socketConfig);
        }

        public ConnectionConfig getConnectionConfig(HttpHost host) {
            return (ConnectionConfig) this.connectionConfigMap.get(host);
        }

        public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
            this.connectionConfigMap.put(host, connectionConfig);
        }
    }

    static class InternalConnectionFactory implements ConnFactory<HttpRoute, ManagedHttpClientConnection> {
        private final ConfigData configData;
        private final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;

        InternalConnectionFactory(ConfigData configData, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
            if (configData == null) {
                configData = new ConfigData();
            }
            this.configData = configData;
            if (connFactory == null) {
                connFactory = ManagedHttpClientConnectionFactory.INSTANCE;
            }
            this.connFactory = connFactory;
        }

        public ManagedHttpClientConnection create(HttpRoute route) throws IOException {
            ConnectionConfig config = null;
            if (route.getProxyHost() != null) {
                config = this.configData.getConnectionConfig(route.getProxyHost());
            }
            if (config == null) {
                config = this.configData.getConnectionConfig(route.getTargetHost());
            }
            if (config == null) {
                config = this.configData.getDefaultConnectionConfig();
            }
            if (config == null) {
                config = ConnectionConfig.DEFAULT;
            }
            return (ManagedHttpClientConnection) this.connFactory.create(route, config);
        }
    }

    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.create().register(HttpHost.DEFAULT_SCHEME_NAME, PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
    }

    public PoolingHttpClientConnectionManager() {
        this(getDefaultRegistry());
    }

    public PoolingHttpClientConnectionManager(long timeToLive, TimeUnit tunit) {
        this(getDefaultRegistry(), null, null, null, timeToLive, tunit);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        this(socketFactoryRegistry, null, null);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, DnsResolver dnsResolver) {
        this(socketFactoryRegistry, null, dnsResolver);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
        this(socketFactoryRegistry, connFactory, null);
    }

    public PoolingHttpClientConnectionManager(HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
        this(getDefaultRegistry(), connFactory, null);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, DnsResolver dnsResolver) {
        this(socketFactoryRegistry, connFactory, null, dnsResolver, -1, TimeUnit.MILLISECONDS);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, SchemePortResolver schemePortResolver, DnsResolver dnsResolver, long timeToLive, TimeUnit tunit) {
        this(new DefaultHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver), (HttpConnectionFactory) connFactory, timeToLive, tunit);
    }

    public PoolingHttpClientConnectionManager(HttpClientConnectionOperator httpClientConnectionOperator, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, long timeToLive, TimeUnit tunit) {
        this.log = LogFactory.getLog(getClass());
        this.configData = new ConfigData();
        this.pool = new CPool(new InternalConnectionFactory(this.configData, connFactory), 2, 20, timeToLive, tunit);
        this.pool.setValidateAfterInactivity(2000);
        this.connectionOperator = (HttpClientConnectionOperator) Args.notNull(httpClientConnectionOperator, "HttpClientConnectionOperator");
        this.isShutDown = new AtomicBoolean(false);
    }

    PoolingHttpClientConnectionManager(CPool pool, Lookup<ConnectionSocketFactory> socketFactoryRegistry, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        this.log = LogFactory.getLog(getClass());
        this.configData = new ConfigData();
        this.pool = pool;
        this.connectionOperator = new DefaultHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver);
        this.isShutDown = new AtomicBoolean(false);
    }

    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        shutdown();
    }

    private String format(HttpRoute route, Object state) {
        StringBuilder buf = new StringBuilder();
        buf.append("[route: ").append(route).append("]");
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    private String formatStats(HttpRoute route) {
        StringBuilder buf = new StringBuilder();
        PoolStats totals = this.pool.getTotalStats();
        PoolStats stats = this.pool.getStats(route);
        buf.append("[total kept alive: ").append(totals.getAvailable()).append("; ");
        buf.append("route allocated: ").append(stats.getLeased() + stats.getAvailable());
        buf.append(" of ").append(stats.getMax()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        return buf.toString();
    }

    private String format(CPoolEntry entry) {
        StringBuilder buf = new StringBuilder();
        buf.append("[id: ").append(entry.getId()).append("]");
        buf.append("[route: ").append(entry.getRoute()).append("]");
        Object state = entry.getState();
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    public ConnectionRequest requestConnection(HttpRoute route, Object state) {
        Args.notNull(route, "HTTP route");
        if (this.log.isDebugEnabled()) {
            this.log.debug("Connection request: " + format(route, state) + formatStats(route));
        }
        final Future<CPoolEntry> future = this.pool.lease(route, state, null);
        return new ConnectionRequest() {
            public boolean cancel() {
                return future.cancel(true);
            }

            public HttpClientConnection get(long timeout, TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                return PoolingHttpClientConnectionManager.this.leaseConnection(future, timeout, tunit);
            }
        };
    }

    protected HttpClientConnection leaseConnection(Future<CPoolEntry> future, long timeout, TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        try {
            CPoolEntry entry = (CPoolEntry) future.get(timeout, tunit);
            if (entry == null || future.isCancelled()) {
                throw new InterruptedException();
            }
            Asserts.check(entry.getConnection() != null, "Pool entry with no connection");
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connection leased: " + format(entry) + formatStats((HttpRoute) entry.getRoute()));
            }
            return CPoolProxy.newProxy(entry);
        } catch (TimeoutException e) {
            throw new ConnectionPoolTimeoutException("Timeout waiting for connection from pool");
        }
    }

    public void releaseConnection(org.apache.http.HttpClientConnection r12, java.lang.Object r13, long r14, java.util.concurrent.TimeUnit r16) {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.utils.BlockUtils.getBlockByInsn(BlockUtils.java:172)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:90)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r11 = this;
        r4 = "Managed connection";
        org.apache.http.util.Args.notNull(r12, r4);
        monitor-enter(r12);
        r2 = org.apache.http.impl.conn.CPoolProxy.detach(r12);	 Catch:{ all -> 0x00c3 }
        if (r2 != 0) goto L_0x000e;	 Catch:{ all -> 0x00c3 }
    L_0x000c:
        monitor-exit(r12);	 Catch:{ all -> 0x00c3 }
    L_0x000d:
        return;	 Catch:{ all -> 0x00c3 }
    L_0x000e:
        r0 = r2.getConnection();	 Catch:{ all -> 0x00c3 }
        r0 = (org.apache.http.conn.ManagedHttpClientConnection) r0;	 Catch:{ all -> 0x00c3 }
        r4 = r0.isOpen();	 Catch:{ all -> 0x00cf }
        if (r4 == 0) goto L_0x007c;	 Catch:{ all -> 0x00cf }
    L_0x001a:
        if (r16 == 0) goto L_0x00c6;	 Catch:{ all -> 0x00cf }
    L_0x001c:
        r1 = r16;	 Catch:{ all -> 0x00cf }
    L_0x001e:
        r2.setState(r13);	 Catch:{ all -> 0x00cf }
        r2.updateExpiry(r14, r1);	 Catch:{ all -> 0x00cf }
        r4 = r11.log;	 Catch:{ all -> 0x00cf }
        r4 = r4.isDebugEnabled();	 Catch:{ all -> 0x00cf }
        if (r4 == 0) goto L_0x007c;	 Catch:{ all -> 0x00cf }
    L_0x002c:
        r4 = 0;	 Catch:{ all -> 0x00cf }
        r4 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1));	 Catch:{ all -> 0x00cf }
        if (r4 <= 0) goto L_0x00ca;	 Catch:{ all -> 0x00cf }
    L_0x0032:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00cf }
        r4.<init>();	 Catch:{ all -> 0x00cf }
        r5 = "for ";	 Catch:{ all -> 0x00cf }
        r4 = r4.append(r5);	 Catch:{ all -> 0x00cf }
        r6 = r1.toMillis(r14);	 Catch:{ all -> 0x00cf }
        r6 = (double) r6;	 Catch:{ all -> 0x00cf }
        r8 = 4652007308841189376; // 0x408f400000000000 float:0.0 double:1000.0;	 Catch:{ all -> 0x00cf }
        r6 = r6 / r8;	 Catch:{ all -> 0x00cf }
        r4 = r4.append(r6);	 Catch:{ all -> 0x00cf }
        r5 = " seconds";	 Catch:{ all -> 0x00cf }
        r4 = r4.append(r5);	 Catch:{ all -> 0x00cf }
        r3 = r4.toString();	 Catch:{ all -> 0x00cf }
    L_0x0056:
        r4 = r11.log;	 Catch:{ all -> 0x00cf }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00cf }
        r5.<init>();	 Catch:{ all -> 0x00cf }
        r6 = "Connection ";	 Catch:{ all -> 0x00cf }
        r5 = r5.append(r6);	 Catch:{ all -> 0x00cf }
        r6 = r11.format(r2);	 Catch:{ all -> 0x00cf }
        r5 = r5.append(r6);	 Catch:{ all -> 0x00cf }
        r6 = " can be kept alive ";	 Catch:{ all -> 0x00cf }
        r5 = r5.append(r6);	 Catch:{ all -> 0x00cf }
        r5 = r5.append(r3);	 Catch:{ all -> 0x00cf }
        r5 = r5.toString();	 Catch:{ all -> 0x00cf }
        r4.debug(r5);	 Catch:{ all -> 0x00cf }
    L_0x007c:
        r5 = r11.pool;	 Catch:{ all -> 0x00c3 }
        r4 = r0.isOpen();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00c3 }
    L_0x0084:
        r4 = r2.isRouteComplete();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00c3 }
    L_0x008a:
        r4 = 1;	 Catch:{ all -> 0x00c3 }
    L_0x008b:
        r5.release(r2, r4);	 Catch:{ all -> 0x00c3 }
        r4 = r11.log;	 Catch:{ all -> 0x00c3 }
        r4 = r4.isDebugEnabled();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x00c0;	 Catch:{ all -> 0x00c3 }
    L_0x0096:
        r5 = r11.log;	 Catch:{ all -> 0x00c3 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c3 }
        r4.<init>();	 Catch:{ all -> 0x00c3 }
        r6 = "Connection released: ";	 Catch:{ all -> 0x00c3 }
        r4 = r4.append(r6);	 Catch:{ all -> 0x00c3 }
        r6 = r11.format(r2);	 Catch:{ all -> 0x00c3 }
        r6 = r4.append(r6);	 Catch:{ all -> 0x00c3 }
        r4 = r2.getRoute();	 Catch:{ all -> 0x00c3 }
        r4 = (org.apache.http.conn.routing.HttpRoute) r4;	 Catch:{ all -> 0x00c3 }
        r4 = r11.formatStats(r4);	 Catch:{ all -> 0x00c3 }
        r4 = r6.append(r4);	 Catch:{ all -> 0x00c3 }
        r4 = r4.toString();	 Catch:{ all -> 0x00c3 }
        r5.debug(r4);	 Catch:{ all -> 0x00c3 }
    L_0x00c0:
        monitor-exit(r12);	 Catch:{ all -> 0x00c3 }
        goto L_0x000d;	 Catch:{ all -> 0x00c3 }
    L_0x00c3:
        r4 = move-exception;	 Catch:{ all -> 0x00c3 }
        monitor-exit(r12);	 Catch:{ all -> 0x00c3 }
        throw r4;
    L_0x00c6:
        r1 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ all -> 0x00cf }
        goto L_0x001e;	 Catch:{ all -> 0x00cf }
    L_0x00ca:
        r3 = "indefinitely";	 Catch:{ all -> 0x00cf }
        goto L_0x0056;
    L_0x00cd:
        r4 = 0;
        goto L_0x008b;
    L_0x00cf:
        r4 = move-exception;
        r5 = r4;
        r6 = r11.pool;	 Catch:{ all -> 0x00c3 }
        r4 = r0.isOpen();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x0116;	 Catch:{ all -> 0x00c3 }
    L_0x00d9:
        r4 = r2.isRouteComplete();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x0116;	 Catch:{ all -> 0x00c3 }
    L_0x00df:
        r4 = 1;	 Catch:{ all -> 0x00c3 }
    L_0x00e0:
        r6.release(r2, r4);	 Catch:{ all -> 0x00c3 }
        r4 = r11.log;	 Catch:{ all -> 0x00c3 }
        r4 = r4.isDebugEnabled();	 Catch:{ all -> 0x00c3 }
        if (r4 == 0) goto L_0x0115;	 Catch:{ all -> 0x00c3 }
    L_0x00eb:
        r6 = r11.log;	 Catch:{ all -> 0x00c3 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c3 }
        r4.<init>();	 Catch:{ all -> 0x00c3 }
        r7 = "Connection released: ";	 Catch:{ all -> 0x00c3 }
        r4 = r4.append(r7);	 Catch:{ all -> 0x00c3 }
        r7 = r11.format(r2);	 Catch:{ all -> 0x00c3 }
        r7 = r4.append(r7);	 Catch:{ all -> 0x00c3 }
        r4 = r2.getRoute();	 Catch:{ all -> 0x00c3 }
        r4 = (org.apache.http.conn.routing.HttpRoute) r4;	 Catch:{ all -> 0x00c3 }
        r4 = r11.formatStats(r4);	 Catch:{ all -> 0x00c3 }
        r4 = r7.append(r4);	 Catch:{ all -> 0x00c3 }
        r4 = r4.toString();	 Catch:{ all -> 0x00c3 }
        r6.debug(r4);	 Catch:{ all -> 0x00c3 }
    L_0x0115:
        throw r5;	 Catch:{ all -> 0x00c3 }
    L_0x0116:
        r4 = 0;
        goto L_0x00e0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.conn.PoolingHttpClientConnectionManager.releaseConnection(org.apache.http.HttpClientConnection, java.lang.Object, long, java.util.concurrent.TimeUnit):void");
    }

    public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {
        ManagedHttpClientConnection conn;
        HttpHost host;
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            conn = (ManagedHttpClientConnection) CPoolProxy.getPoolEntry(managedConn).getConnection();
        }
        if (route.getProxyHost() != null) {
            host = route.getProxyHost();
        } else {
            host = route.getTargetHost();
        }
        InetSocketAddress localAddress = route.getLocalSocketAddress();
        SocketConfig socketConfig = this.configData.getSocketConfig(host);
        if (socketConfig == null) {
            socketConfig = this.configData.getDefaultSocketConfig();
        }
        if (socketConfig == null) {
            socketConfig = SocketConfig.DEFAULT;
        }
        this.connectionOperator.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
    }

    public void upgrade(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        ManagedHttpClientConnection conn;
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            conn = (ManagedHttpClientConnection) CPoolProxy.getPoolEntry(managedConn).getConnection();
        }
        this.connectionOperator.upgrade(conn, route.getTargetHost(), context);
    }

    public void routeComplete(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            CPoolProxy.getPoolEntry(managedConn).markRouteComplete();
        }
    }

    public void shutdown() {
        if (this.isShutDown.compareAndSet(false, true)) {
            this.log.debug("Connection manager is shutting down");
            try {
                this.pool.shutdown();
            } catch (IOException ex) {
                this.log.debug("I/O exception shutting down connection manager", ex);
            }
            this.log.debug("Connection manager shut down");
        }
    }

    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Closing connections idle longer than " + idleTimeout + " " + tunit);
        }
        this.pool.closeIdle(idleTimeout, tunit);
    }

    public void closeExpiredConnections() {
        this.log.debug("Closing expired connections");
        this.pool.closeExpired();
    }

    public int getMaxTotal() {
        return this.pool.getMaxTotal();
    }

    public void setMaxTotal(int max) {
        this.pool.setMaxTotal(max);
    }

    public int getDefaultMaxPerRoute() {
        return this.pool.getDefaultMaxPerRoute();
    }

    public void setDefaultMaxPerRoute(int max) {
        this.pool.setDefaultMaxPerRoute(max);
    }

    public int getMaxPerRoute(HttpRoute route) {
        return this.pool.getMaxPerRoute(route);
    }

    public void setMaxPerRoute(HttpRoute route, int max) {
        this.pool.setMaxPerRoute(route, max);
    }

    public PoolStats getTotalStats() {
        return this.pool.getTotalStats();
    }

    public PoolStats getStats(HttpRoute route) {
        return this.pool.getStats(route);
    }

    public Set<HttpRoute> getRoutes() {
        return this.pool.getRoutes();
    }

    public SocketConfig getDefaultSocketConfig() {
        return this.configData.getDefaultSocketConfig();
    }

    public void setDefaultSocketConfig(SocketConfig defaultSocketConfig) {
        this.configData.setDefaultSocketConfig(defaultSocketConfig);
    }

    public ConnectionConfig getDefaultConnectionConfig() {
        return this.configData.getDefaultConnectionConfig();
    }

    public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig) {
        this.configData.setDefaultConnectionConfig(defaultConnectionConfig);
    }

    public SocketConfig getSocketConfig(HttpHost host) {
        return this.configData.getSocketConfig(host);
    }

    public void setSocketConfig(HttpHost host, SocketConfig socketConfig) {
        this.configData.setSocketConfig(host, socketConfig);
    }

    public ConnectionConfig getConnectionConfig(HttpHost host) {
        return this.configData.getConnectionConfig(host);
    }

    public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
        this.configData.setConnectionConfig(host, connectionConfig);
    }

    public int getValidateAfterInactivity() {
        return this.pool.getValidateAfterInactivity();
    }

    public void setValidateAfterInactivity(int ms) {
        this.pool.setValidateAfterInactivity(ms);
    }
}
