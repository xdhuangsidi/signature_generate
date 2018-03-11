package org.apache.commons.logging.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

public class SimpleLog implements Log, Serializable {
    protected static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS zzz";
    public static final int LOG_LEVEL_ALL = 0;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_FATAL = 6;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_OFF = 7;
    public static final int LOG_LEVEL_TRACE = 1;
    public static final int LOG_LEVEL_WARN = 4;
    static Class class$java$lang$Thread = null;
    static Class class$org$apache$commons$logging$impl$SimpleLog = null;
    protected static DateFormat dateFormatter = null;
    protected static volatile String dateTimeFormat = null;
    private static final long serialVersionUID = 136942970684951178L;
    protected static volatile boolean showDateTime = false;
    protected static volatile boolean showLogName = false;
    protected static volatile boolean showShortName = false;
    protected static final Properties simpleLogProps = new Properties();
    protected static final String systemPrefix = "org.apache.commons.logging.simplelog.";
    protected volatile int currentLogLevel;
    protected volatile String logName = null;
    private volatile String shortLogName = null;

    final class AnonymousClass1 implements PrivilegedAction {
        private final String val$name;

        AnonymousClass1(String str) {
            this.val$name = str;
        }

        public Object run() {
            ClassLoader threadCL = SimpleLog.access$000();
            if (threadCL != null) {
                return threadCL.getResourceAsStream(this.val$name);
            }
            return ClassLoader.getSystemResourceAsStream(this.val$name);
        }
    }

    static ClassLoader access$000() {
        return getContextClassLoader();
    }

    static {
        showLogName = false;
        showShortName = true;
        showDateTime = false;
        dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
        dateFormatter = null;
        InputStream in = getResourceAsStream("simplelog.properties");
        if (in != null) {
            try {
                simpleLogProps.load(in);
                in.close();
            } catch (IOException e) {
            }
        }
        showLogName = getBooleanProperty("org.apache.commons.logging.simplelog.showlogname", showLogName);
        showShortName = getBooleanProperty("org.apache.commons.logging.simplelog.showShortLogname", showShortName);
        showDateTime = getBooleanProperty("org.apache.commons.logging.simplelog.showdatetime", showDateTime);
        if (showDateTime) {
            dateTimeFormat = getStringProperty("org.apache.commons.logging.simplelog.dateTimeFormat", dateTimeFormat);
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            } catch (IllegalArgumentException e2) {
                dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
        }
    }

    private static String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
        }
        return prop == null ? simpleLogProps.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : prop;
    }

    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : "true".equalsIgnoreCase(prop);
    }

    public SimpleLog(String name) {
        this.logName = name;
        setLevel(3);
        String lvl = getStringProperty(new StringBuffer().append("org.apache.commons.logging.simplelog.log.").append(this.logName).toString());
        int i = String.valueOf(name).lastIndexOf(".");
        while (lvl == null && i > -1) {
            name = name.substring(0, i);
            lvl = getStringProperty(new StringBuffer().append("org.apache.commons.logging.simplelog.log.").append(name).toString());
            i = String.valueOf(name).lastIndexOf(".");
        }
        if (lvl == null) {
            lvl = getStringProperty("org.apache.commons.logging.simplelog.defaultlog");
        }
        if ("all".equalsIgnoreCase(lvl)) {
            setLevel(0);
        } else if ("trace".equalsIgnoreCase(lvl)) {
            setLevel(1);
        } else if ("debug".equalsIgnoreCase(lvl)) {
            setLevel(2);
        } else if ("info".equalsIgnoreCase(lvl)) {
            setLevel(3);
        } else if ("warn".equalsIgnoreCase(lvl)) {
            setLevel(4);
        } else if ("error".equalsIgnoreCase(lvl)) {
            setLevel(5);
        } else if ("fatal".equalsIgnoreCase(lvl)) {
            setLevel(6);
        } else if ("off".equalsIgnoreCase(lvl)) {
            setLevel(7);
        }
    }

    public void setLevel(int currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }

    public int getLevel() {
        return this.currentLogLevel;
    }

    protected void log(int type, Object message, Throwable t) {
        StringBuffer buf = new StringBuffer();
        if (showDateTime) {
            String dateText;
            Date now = new Date();
            synchronized (dateFormatter) {
                dateText = dateFormatter.format(now);
            }
            buf.append(dateText);
            buf.append(" ");
        }
        switch (type) {
            case 1:
                buf.append("[TRACE] ");
                break;
            case 2:
                buf.append("[DEBUG] ");
                break;
            case 3:
                buf.append("[INFO] ");
                break;
            case 4:
                buf.append("[WARN] ");
                break;
            case 5:
                buf.append("[ERROR] ");
                break;
            case 6:
                buf.append("[FATAL] ");
                break;
        }
        if (showShortName) {
            if (this.shortLogName == null) {
                String slName = this.logName.substring(this.logName.lastIndexOf(".") + 1);
                this.shortLogName = slName.substring(slName.lastIndexOf("/") + 1);
            }
            buf.append(String.valueOf(this.shortLogName)).append(" - ");
        } else if (showLogName) {
            buf.append(String.valueOf(this.logName)).append(" - ");
        }
        buf.append(String.valueOf(message));
        if (t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");
            StringWriter sw = new StringWriter(1024);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }
        write(buf);
    }

    protected void write(StringBuffer buffer) {
        System.err.println(buffer.toString());
    }

    protected boolean isLevelEnabled(int logLevel) {
        return logLevel >= this.currentLogLevel;
    }

    public final void debug(Object message) {
        if (isLevelEnabled(2)) {
            log(2, message, null);
        }
    }

    public final void debug(Object message, Throwable t) {
        if (isLevelEnabled(2)) {
            log(2, message, t);
        }
    }

    public final void trace(Object message) {
        if (isLevelEnabled(1)) {
            log(1, message, null);
        }
    }

    public final void trace(Object message, Throwable t) {
        if (isLevelEnabled(1)) {
            log(1, message, t);
        }
    }

    public final void info(Object message) {
        if (isLevelEnabled(3)) {
            log(3, message, null);
        }
    }

    public final void info(Object message, Throwable t) {
        if (isLevelEnabled(3)) {
            log(3, message, t);
        }
    }

    public final void warn(Object message) {
        if (isLevelEnabled(4)) {
            log(4, message, null);
        }
    }

    public final void warn(Object message, Throwable t) {
        if (isLevelEnabled(4)) {
            log(4, message, t);
        }
    }

    public final void error(Object message) {
        if (isLevelEnabled(5)) {
            log(5, message, null);
        }
    }

    public final void error(Object message, Throwable t) {
        if (isLevelEnabled(5)) {
            log(5, message, t);
        }
    }

    public final void fatal(Object message) {
        if (isLevelEnabled(6)) {
            log(6, message, null);
        }
    }

    public final void fatal(Object message, Throwable t) {
        if (isLevelEnabled(6)) {
            log(6, message, t);
        }
    }

    public final boolean isDebugEnabled() {
        return isLevelEnabled(2);
    }

    public final boolean isErrorEnabled() {
        return isLevelEnabled(5);
    }

    public final boolean isFatalEnabled() {
        return isLevelEnabled(6);
    }

    public final boolean isInfoEnabled() {
        return isLevelEnabled(3);
    }

    public final boolean isTraceEnabled() {
        return isLevelEnabled(1);
    }

    public final boolean isWarnEnabled() {
        return isLevelEnabled(4);
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = null;
        try {
            Class cls;
            if (class$java$lang$Thread == null) {
                Class class$ = class$("java.lang.Thread");
                class$java$lang$Thread = class$;
                cls = class$;
            } else {
                cls = class$java$lang$Thread;
            }
            classLoader = (ClassLoader) cls.getMethod("getContextClassLoader", (Class[]) null).invoke(Thread.currentThread(), (Class[]) null);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e2) {
            if (!(e2.getTargetException() instanceof SecurityException)) {
                throw new LogConfigurationException("Unexpected InvocationTargetException", e2.getTargetException());
            }
        } catch (NoSuchMethodException e3) {
        }
        if (classLoader != null) {
            return classLoader;
        }
        if (class$org$apache$commons$logging$impl$SimpleLog == null) {
            class$ = class$("org.apache.commons.logging.impl.SimpleLog");
            class$org$apache$commons$logging$impl$SimpleLog = class$;
        } else {
            class$ = class$org$apache$commons$logging$impl$SimpleLog;
        }
        return class$.getClassLoader();
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static InputStream getResourceAsStream(String name) {
        return (InputStream) AccessController.doPrivileged(new AnonymousClass1(name));
    }
}
