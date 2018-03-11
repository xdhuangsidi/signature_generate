package org.apache.commons.logging.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

public class LogFactoryImpl extends LogFactory {
    public static final String ALLOW_FLAWED_CONTEXT_PROPERTY = "org.apache.commons.logging.Log.allowFlawedContext";
    public static final String ALLOW_FLAWED_DISCOVERY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedDiscovery";
    public static final String ALLOW_FLAWED_HIERARCHY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedHierarchy";
    private static final String LOGGING_IMPL_JDK14_LOGGER = "org.apache.commons.logging.impl.Jdk14Logger";
    private static final String LOGGING_IMPL_LOG4J_LOGGER = "org.apache.commons.logging.impl.Log4JLogger";
    private static final String LOGGING_IMPL_LUMBERJACK_LOGGER = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
    private static final String LOGGING_IMPL_SIMPLE_LOGGER = "org.apache.commons.logging.impl.SimpleLog";
    public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";
    protected static final String LOG_PROPERTY_OLD = "org.apache.commons.logging.log";
    private static final String PKG_IMPL = "org.apache.commons.logging.impl.";
    private static final int PKG_LEN = PKG_IMPL.length();
    static Class class$java$lang$String;
    static Class class$org$apache$commons$logging$Log;
    static Class class$org$apache$commons$logging$LogFactory;
    static Class class$org$apache$commons$logging$impl$LogFactoryImpl;
    private static final String[] classesToDiscover = new String[]{LOGGING_IMPL_LOG4J_LOGGER, LOGGING_IMPL_JDK14_LOGGER, LOGGING_IMPL_LUMBERJACK_LOGGER, LOGGING_IMPL_SIMPLE_LOGGER};
    private boolean allowFlawedContext;
    private boolean allowFlawedDiscovery;
    private boolean allowFlawedHierarchy;
    protected Hashtable attributes = new Hashtable();
    private String diagnosticPrefix;
    protected Hashtable instances = new Hashtable();
    private String logClassName;
    protected Constructor logConstructor = null;
    protected Class[] logConstructorSignature;
    protected Method logMethod;
    protected Class[] logMethodSignature;
    private boolean useTCCL = true;

    final class AnonymousClass2 implements PrivilegedAction {
        private final String val$def;
        private final String val$key;

        AnonymousClass2(String str, String str2) {
            this.val$key = str;
            this.val$def = str2;
        }

        public Object run() {
            return System.getProperty(this.val$key, this.val$def);
        }
    }

    class AnonymousClass3 implements PrivilegedAction {
        private final LogFactoryImpl this$0;
        private final ClassLoader val$cl;

        AnonymousClass3(LogFactoryImpl logFactoryImpl, ClassLoader classLoader) {
            this.this$0 = logFactoryImpl;
            this.val$cl = classLoader;
        }

        public Object run() {
            return this.val$cl.getParent();
        }
    }

    static ClassLoader access$000() throws LogConfigurationException {
        return LogFactory.directGetContextClassLoader();
    }

    public LogFactoryImpl() {
        Class class$;
        Class[] clsArr = new Class[1];
        if (class$java$lang$String == null) {
            class$ = class$("java.lang.String");
            class$java$lang$String = class$;
        } else {
            class$ = class$java$lang$String;
        }
        clsArr[0] = class$;
        this.logConstructorSignature = clsArr;
        this.logMethod = null;
        clsArr = new Class[1];
        if (class$org$apache$commons$logging$LogFactory == null) {
            class$ = class$(LogFactory.FACTORY_PROPERTY);
            class$org$apache$commons$logging$LogFactory = class$;
        } else {
            class$ = class$org$apache$commons$logging$LogFactory;
        }
        clsArr[0] = class$;
        this.logMethodSignature = clsArr;
        initDiagnostics();
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String[] getAttributeNames() {
        return (String[]) this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log) this.instances.get(name);
        if (instance != null) {
            return instance;
        }
        instance = newInstance(name);
        this.instances.put(name, instance);
        return instance;
    }

    public void release() {
        logDiagnostic("Releasing all known loggers");
        this.instances.clear();
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        if (this.logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
        if (name.equals(LogFactory.TCCL_KEY)) {
            boolean z = value != null && Boolean.valueOf(value.toString()).booleanValue();
            this.useTCCL = z;
        }
    }

    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }

    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }

    protected static ClassLoader getClassLoader(Class clazz) {
        return LogFactory.getClassLoader(clazz);
    }

    private void initDiagnostics() {
        String classLoaderName;
        ClassLoader classLoader = getClassLoader(getClass());
        if (classLoader == null) {
            try {
                classLoaderName = "BOOTLOADER";
            } catch (SecurityException e) {
                classLoaderName = "UNKNOWN";
            }
        } else {
            classLoaderName = LogFactory.objectId(classLoader);
        }
        this.diagnosticPrefix = new StringBuffer().append("[LogFactoryImpl@").append(System.identityHashCode(this)).append(" from ").append(classLoaderName).append("] ").toString();
    }

    protected void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            LogFactory.logRawDiagnostic(new StringBuffer().append(this.diagnosticPrefix).append(msg).toString());
        }
    }

    protected String getLogClassName() {
        if (this.logClassName == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logClassName;
    }

    protected Constructor getLogConstructor() throws LogConfigurationException {
        if (this.logConstructor == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logConstructor;
    }

    protected boolean isJdk13LumberjackAvailable() {
        return isLogLibraryAvailable("Jdk13Lumberjack", LOGGING_IMPL_LUMBERJACK_LOGGER);
    }

    protected boolean isJdk14Available() {
        return isLogLibraryAvailable("Jdk14", LOGGING_IMPL_JDK14_LOGGER);
    }

    protected boolean isLog4JAvailable() {
        return isLogLibraryAvailable("Log4J", LOGGING_IMPL_LOG4J_LOGGER);
    }

    protected Log newInstance(String name) throws LogConfigurationException {
        try {
            Log instance;
            if (this.logConstructor == null) {
                instance = discoverLogImplementation(name);
            } else {
                instance = (Log) this.logConstructor.newInstance(new Object[]{name});
            }
            if (this.logMethod != null) {
                this.logMethod.invoke(instance, new Object[]{this});
            }
            return instance;
        } catch (LogConfigurationException lce) {
            throw lce;
        } catch (InvocationTargetException e) {
            Throwable e2 = e;
            Throwable c = e2.getTargetException();
            if (c != null) {
                e2 = c;
            }
            throw new LogConfigurationException(e2);
        } catch (Throwable t) {
            LogFactory.handleThrowable(t);
            LogConfigurationException logConfigurationException = new LogConfigurationException(t);
        }
    }

    private static ClassLoader getContextClassLoaderInternal() throws LogConfigurationException {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return LogFactoryImpl.access$000();
            }
        });
    }

    private static String getSystemProperty(String key, String def) throws SecurityException {
        return (String) AccessController.doPrivileged(new AnonymousClass2(key, def));
    }

    private ClassLoader getParentClassLoader(ClassLoader cl) {
        try {
            return (ClassLoader) AccessController.doPrivileged(new AnonymousClass3(this, cl));
        } catch (SecurityException e) {
            logDiagnostic("[SECURITY] Unable to obtain parent classloader");
            return null;
        }
    }

    private boolean isLogLibraryAvailable(String name, String classname) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer().append("Checking for '").append(name).append("'.").toString());
        }
        try {
            if (createLogFromClass(classname, getClass().getName(), false) != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer().append("Found '").append(name).append("'.").toString());
                }
                return true;
            } else if (!isDiagnosticsEnabled()) {
                return false;
            } else {
                logDiagnostic(new StringBuffer().append("Did not find '").append(name).append("'.").toString());
                return false;
            }
        } catch (LogConfigurationException e) {
            if (!isDiagnosticsEnabled()) {
                return false;
            }
            logDiagnostic(new StringBuffer().append("Logging system '").append(name).append("' is available but not useable.").toString());
            return false;
        }
    }

    private String getConfigurationValue(String property) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer().append("[ENV] Trying to get configuration for item ").append(property).toString());
        }
        Object valueObj = getAttribute(property);
        if (valueObj != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer().append("[ENV] Found LogFactory attribute [").append(valueObj).append("] for ").append(property).toString());
            }
            return valueObj.toString();
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer().append("[ENV] No LogFactory attribute found for ").append(property).toString());
        }
        try {
            String value = getSystemProperty(property, null);
            if (value == null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer().append("[ENV] No system property found for property ").append(property).toString());
                }
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer().append("[ENV] No configuration defined for item ").append(property).toString());
                }
                return null;
            } else if (!isDiagnosticsEnabled()) {
                return value;
            } else {
                logDiagnostic(new StringBuffer().append("[ENV] Found system property [").append(value).append("] for ").append(property).toString());
                return value;
            }
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer().append("[ENV] Security prevented reading system property ").append(property).toString());
            }
        }
    }

    private boolean getBooleanConfiguration(String key, boolean dflt) {
        String val = getConfigurationValue(key);
        return val == null ? dflt : Boolean.valueOf(val).booleanValue();
    }

    private void initConfiguration() {
        this.allowFlawedContext = getBooleanConfiguration(ALLOW_FLAWED_CONTEXT_PROPERTY, true);
        this.allowFlawedDiscovery = getBooleanConfiguration(ALLOW_FLAWED_DISCOVERY_PROPERTY, true);
        this.allowFlawedHierarchy = getBooleanConfiguration(ALLOW_FLAWED_HIERARCHY_PROPERTY, true);
    }

    private Log discoverLogImplementation(String logCategory) throws LogConfigurationException {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Discovering a Log implementation...");
        }
        initConfiguration();
        Log result = null;
        String specifiedLogClassName = findUserSpecifiedLogClassName();
        if (specifiedLogClassName != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer().append("Attempting to load user-specified log class '").append(specifiedLogClassName).append("'...").toString());
            }
            result = createLogFromClass(specifiedLogClassName, logCategory, true);
            if (result != null) {
                return result;
            }
            StringBuffer messageBuffer = new StringBuffer("User-specified log class '");
            messageBuffer.append(specifiedLogClassName);
            messageBuffer.append("' cannot be found or is not useable.");
            informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LOG4J_LOGGER);
            informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_JDK14_LOGGER);
            informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LUMBERJACK_LOGGER);
            informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_SIMPLE_LOGGER);
            throw new LogConfigurationException(messageBuffer.toString());
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("No user-specified Log implementation; performing discovery using the standard supported logging implementations...");
        }
        for (int i = 0; i < classesToDiscover.length && result == null; i++) {
            result = createLogFromClass(classesToDiscover[i], logCategory, true);
        }
        if (result != null) {
            return result;
        }
        throw new LogConfigurationException("No suitable Log implementation");
    }

    private void informUponSimilarName(StringBuffer messageBuffer, String name, String candidate) {
        if (!name.equals(candidate)) {
            if (name.regionMatches(true, 0, candidate, 0, PKG_LEN + 5)) {
                messageBuffer.append(" Did you mean '");
                messageBuffer.append(candidate);
                messageBuffer.append("'?");
            }
        }
    }

    private String findUserSpecifiedLogClassName() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.Log'");
        }
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.log'");
            }
            specifiedClass = (String) getAttribute(LOG_PROPERTY_OLD);
        }
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.Log'");
            }
            try {
                specifiedClass = getSystemProperty(LOG_PROPERTY, null);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer().append("No access allowed to system property 'org.apache.commons.logging.Log' - ").append(e.getMessage()).toString());
                }
            }
        }
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.log'");
            }
            try {
                specifiedClass = getSystemProperty(LOG_PROPERTY_OLD, null);
            } catch (SecurityException e2) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer().append("No access allowed to system property 'org.apache.commons.logging.log' - ").append(e2.getMessage()).toString());
                }
            }
        }
        if (specifiedClass != null) {
            return specifiedClass.trim();
        }
        return specifiedClass;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.apache.commons.logging.Log createLogFromClass(java.lang.String r21, java.lang.String r22, boolean r23) throws org.apache.commons.logging.LogConfigurationException {
        /*
        r20 = this;
        r17 = isDiagnosticsEnabled();
        if (r17 == 0) goto L_0x002a;
    L_0x0006:
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "Attempting to instantiate '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "'";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
    L_0x002a:
        r17 = 1;
        r0 = r17;
        r12 = new java.lang.Object[r0];
        r17 = 0;
        r12[r17] = r22;
        r7 = 0;
        r4 = 0;
        r8 = 0;
        r5 = r20.getBaseClassLoader();
    L_0x003b:
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "Trying to load '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "' from classloader ";
        r17 = r17.append(r18);
        r18 = org.apache.commons.logging.LogFactory.objectId(r5);
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
        r17 = isDiagnosticsEnabled();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        if (r17 == 0) goto L_0x00c6;
    L_0x006d:
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = 46;
        r19 = 47;
        r0 = r21;
        r1 = r18;
        r2 = r19;
        r18 = r0.replace(r1, r2);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = ".class";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r13 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        if (r5 == 0) goto L_0x016a;
    L_0x0090:
        r16 = r5.getResource(r13);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
    L_0x0094:
        if (r16 != 0) goto L_0x0185;
    L_0x0096:
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "Class '";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "' [";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r17 = r0.append(r13);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "] cannot be found.";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
    L_0x00c6:
        r17 = 1;
        r0 = r21;
        r1 = r17;
        r3 = java.lang.Class.forName(r0, r1, r5);	 Catch:{ ClassNotFoundException -> 0x01fa }
    L_0x00d0:
        r0 = r20;
        r0 = r0.logConstructorSignature;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r0;
        r0 = r17;
        r4 = r3.getConstructor(r0);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r10 = r4.newInstance(r12);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r10 instanceof org.apache.commons.logging.Log;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r0;
        if (r17 == 0) goto L_0x02b3;
    L_0x00e6:
        r8 = r3;
        r0 = r10;
        r0 = (org.apache.commons.logging.Log) r0;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r7 = r0;
    L_0x00eb:
        if (r8 == 0) goto L_0x0169;
    L_0x00ed:
        if (r23 == 0) goto L_0x0169;
    L_0x00ef:
        r0 = r21;
        r1 = r20;
        r1.logClassName = r0;
        r0 = r20;
        r0.logConstructor = r4;
        r17 = "setLogFactory";
        r0 = r20;
        r0 = r0.logMethodSignature;	 Catch:{ Throwable -> 0x02d0 }
        r18 = r0;
        r0 = r17;
        r1 = r18;
        r17 = r8.getMethod(r0, r1);	 Catch:{ Throwable -> 0x02d0 }
        r0 = r17;
        r1 = r20;
        r1.logMethod = r0;	 Catch:{ Throwable -> 0x02d0 }
        r17 = new java.lang.StringBuffer;	 Catch:{ Throwable -> 0x02d0 }
        r17.<init>();	 Catch:{ Throwable -> 0x02d0 }
        r18 = "Found method setLogFactory(LogFactory) in '";
        r17 = r17.append(r18);	 Catch:{ Throwable -> 0x02d0 }
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);	 Catch:{ Throwable -> 0x02d0 }
        r18 = "'";
        r17 = r17.append(r18);	 Catch:{ Throwable -> 0x02d0 }
        r17 = r17.toString();	 Catch:{ Throwable -> 0x02d0 }
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);	 Catch:{ Throwable -> 0x02d0 }
    L_0x0133:
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "Log adapter '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "' from classloader ";
        r17 = r17.append(r18);
        r18 = r8.getClassLoader();
        r18 = org.apache.commons.logging.LogFactory.objectId(r18);
        r17 = r17.append(r18);
        r18 = " has been selected for use.";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
    L_0x0169:
        return r7;
    L_0x016a:
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r17 = r0.append(r13);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = ".class";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r16 = java.lang.ClassLoader.getSystemResource(r17);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        goto L_0x0094;
    L_0x0185:
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "Class '";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "' was found at '";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r1 = r16;
        r17 = r0.append(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "'";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        goto L_0x00c6;
    L_0x01b9:
        r6 = move-exception;
        r9 = r6.getMessage();
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "The log adapter '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "' is missing dependencies when loaded via classloader ";
        r17 = r17.append(r18);
        r18 = org.apache.commons.logging.LogFactory.objectId(r5);
        r17 = r17.append(r18);
        r18 = ": ";
        r17 = r17.append(r18);
        r18 = r9.trim();
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
        goto L_0x00eb;
    L_0x01fa:
        r11 = move-exception;
        r9 = r11.getMessage();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "The log adapter '";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "' is not available via classloader ";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = org.apache.commons.logging.LogFactory.objectId(r5);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = ": ";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = r9.trim();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r3 = java.lang.Class.forName(r21);	 Catch:{ ClassNotFoundException -> 0x023f }
        goto L_0x00d0;
    L_0x023f:
        r14 = move-exception;
        r9 = r14.getMessage();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = new java.lang.StringBuffer;	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17.<init>();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "The log adapter '";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = "' is not available via the LogFactoryImpl class classloader: ";
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r18 = r9.trim();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.append(r18);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r17 = r17.toString();	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
        goto L_0x00eb;
    L_0x0272:
        r6 = move-exception;
        r9 = r6.getMessage();
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "The log adapter '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "' is unable to initialize itself when loaded via classloader ";
        r17 = r17.append(r18);
        r18 = org.apache.commons.logging.LogFactory.objectId(r5);
        r17 = r17.append(r18);
        r18 = ": ";
        r17 = r17.append(r18);
        r18 = r9.trim();
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
        goto L_0x00eb;
    L_0x02b3:
        r0 = r20;
        r0.handleFlawedHierarchy(r5, r3);	 Catch:{ NoClassDefFoundError -> 0x01b9, ExceptionInInitializerError -> 0x0272, LogConfigurationException -> 0x02c2, Throwable -> 0x02c4 }
    L_0x02b8:
        if (r5 == 0) goto L_0x00eb;
    L_0x02ba:
        r0 = r20;
        r5 = r0.getParentClassLoader(r5);
        goto L_0x003b;
    L_0x02c2:
        r6 = move-exception;
        throw r6;
    L_0x02c4:
        r15 = move-exception;
        org.apache.commons.logging.LogFactory.handleThrowable(r15);
        r0 = r20;
        r1 = r21;
        r0.handleFlawedDiscovery(r1, r5, r15);
        goto L_0x02b8;
    L_0x02d0:
        r15 = move-exception;
        org.apache.commons.logging.LogFactory.handleThrowable(r15);
        r17 = 0;
        r0 = r17;
        r1 = r20;
        r1.logMethod = r0;
        r17 = new java.lang.StringBuffer;
        r17.<init>();
        r18 = "[INFO] '";
        r17 = r17.append(r18);
        r0 = r17;
        r1 = r21;
        r17 = r0.append(r1);
        r18 = "' from classloader ";
        r17 = r17.append(r18);
        r18 = org.apache.commons.logging.LogFactory.objectId(r5);
        r17 = r17.append(r18);
        r18 = " does not declare optional method ";
        r17 = r17.append(r18);
        r18 = "setLogFactory(LogFactory)";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r0 = r20;
        r1 = r17;
        r0.logDiagnostic(r1);
        goto L_0x0133;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.LogFactoryImpl.createLogFromClass(java.lang.String, java.lang.String, boolean):org.apache.commons.logging.Log");
    }

    private ClassLoader getBaseClassLoader() throws LogConfigurationException {
        Class class$;
        if (class$org$apache$commons$logging$impl$LogFactoryImpl == null) {
            class$ = class$(LogFactory.FACTORY_DEFAULT);
            class$org$apache$commons$logging$impl$LogFactoryImpl = class$;
        } else {
            class$ = class$org$apache$commons$logging$impl$LogFactoryImpl;
        }
        ClassLoader thisClassLoader = getClassLoader(class$);
        if (!this.useTCCL) {
            return thisClassLoader;
        }
        ClassLoader contextClassLoader = getContextClassLoaderInternal();
        ClassLoader baseClassLoader = getLowestClassLoader(contextClassLoader, thisClassLoader);
        if (baseClassLoader != null) {
            if (baseClassLoader != contextClassLoader) {
                if (!this.allowFlawedContext) {
                    throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("Warning: the context classloader is an ancestor of the classloader that loaded LogFactoryImpl; it should be the same or a descendant. The application using commons-logging should ensure the context classloader is used correctly.");
                }
            }
            return baseClassLoader;
        } else if (this.allowFlawedContext) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[WARNING] the context classloader is not part of a parent-child relationship with the classloader that loaded LogFactoryImpl.");
            }
            return contextClassLoader;
        } else {
            throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
        }
    }

    private ClassLoader getLowestClassLoader(ClassLoader c1, ClassLoader c2) {
        if (c1 == null) {
            return c2;
        }
        if (c2 == null) {
            return c1;
        }
        ClassLoader current = c1;
        while (current != null) {
            if (current == c2) {
                return c1;
            }
            current = getParentClassLoader(current);
        }
        current = c2;
        while (current != null) {
            if (current == c1) {
                return c2;
            }
            current = getParentClassLoader(current);
        }
        return null;
    }

    private void handleFlawedDiscovery(String logAdapterClassName, ClassLoader classLoader, Throwable discoveryFlaw) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer().append("Could not instantiate Log '").append(logAdapterClassName).append("' -- ").append(discoveryFlaw.getClass().getName()).append(": ").append(discoveryFlaw.getLocalizedMessage()).toString());
            if (discoveryFlaw instanceof InvocationTargetException) {
                Throwable cause = ((InvocationTargetException) discoveryFlaw).getTargetException();
                if (cause != null) {
                    logDiagnostic(new StringBuffer().append("... InvocationTargetException: ").append(cause.getClass().getName()).append(": ").append(cause.getLocalizedMessage()).toString());
                    if (cause instanceof ExceptionInInitializerError) {
                        Throwable cause2 = ((ExceptionInInitializerError) cause).getException();
                        if (cause2 != null) {
                            StringWriter sw = new StringWriter();
                            cause2.printStackTrace(new PrintWriter(sw, true));
                            logDiagnostic(new StringBuffer().append("... ExceptionInInitializerError: ").append(sw.toString()).toString());
                        }
                    }
                }
            }
        }
        if (!this.allowFlawedDiscovery) {
            throw new LogConfigurationException(discoveryFlaw);
        }
    }

    private void handleFlawedHierarchy(ClassLoader badClassLoader, Class badClass) throws LogConfigurationException {
        Class class$;
        boolean implementsLog = false;
        if (class$org$apache$commons$logging$Log == null) {
            class$ = class$(LOG_PROPERTY);
            class$org$apache$commons$logging$Log = class$;
        } else {
            class$ = class$org$apache$commons$logging$Log;
        }
        String logInterfaceName = class$.getName();
        Class[] interfaces = badClass.getInterfaces();
        for (Class class$2 : interfaces) {
            if (logInterfaceName.equals(class$2.getName())) {
                implementsLog = true;
                break;
            }
        }
        StringBuffer msg;
        if (implementsLog) {
            if (isDiagnosticsEnabled()) {
                try {
                    if (class$org$apache$commons$logging$Log == null) {
                        class$2 = class$(LOG_PROPERTY);
                        class$org$apache$commons$logging$Log = class$2;
                    } else {
                        class$2 = class$org$apache$commons$logging$Log;
                    }
                    logDiagnostic(new StringBuffer().append("Class '").append(badClass.getName()).append("' was found in classloader ").append(LogFactory.objectId(badClassLoader)).append(". It is bound to a Log interface which is not").append(" the one loaded from classloader ").append(LogFactory.objectId(getClassLoader(class$2))).toString());
                } catch (Throwable t) {
                    LogFactory.handleThrowable(t);
                    logDiagnostic(new StringBuffer().append("Error while trying to output diagnostics about bad class '").append(badClass).append("'").toString());
                }
            }
            if (!this.allowFlawedHierarchy) {
                msg = new StringBuffer();
                msg.append("Terminating logging for this context ");
                msg.append("due to bad log hierarchy. ");
                msg.append("You have more than one version of '");
                if (class$org$apache$commons$logging$Log == null) {
                    class$2 = class$(LOG_PROPERTY);
                    class$org$apache$commons$logging$Log = class$2;
                } else {
                    class$2 = class$org$apache$commons$logging$Log;
                }
                msg.append(class$2.getName());
                msg.append("' visible.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                }
                throw new LogConfigurationException(msg.toString());
            } else if (isDiagnosticsEnabled()) {
                msg = new StringBuffer();
                msg.append("Warning: bad log hierarchy. ");
                msg.append("You have more than one version of '");
                if (class$org$apache$commons$logging$Log == null) {
                    class$2 = class$(LOG_PROPERTY);
                    class$org$apache$commons$logging$Log = class$2;
                } else {
                    class$2 = class$org$apache$commons$logging$Log;
                }
                msg.append(class$2.getName());
                msg.append("' visible.");
                logDiagnostic(msg.toString());
            }
        } else if (!this.allowFlawedDiscovery) {
            msg = new StringBuffer();
            msg.append("Terminating logging for this context. ");
            msg.append("Log class '");
            msg.append(badClass.getName());
            msg.append("' does not implement the Log interface.");
            if (isDiagnosticsEnabled()) {
                logDiagnostic(msg.toString());
            }
            throw new LogConfigurationException(msg.toString());
        } else if (isDiagnosticsEnabled()) {
            msg = new StringBuffer();
            msg.append("[WARNING] Log class '");
            msg.append(badClass.getName());
            msg.append("' does not implement the Log interface.");
            logDiagnostic(msg.toString());
        }
    }
}
