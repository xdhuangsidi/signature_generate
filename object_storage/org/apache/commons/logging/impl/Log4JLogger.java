package org.apache.commons.logging.impl;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.http.client.methods.HttpTrace;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class Log4JLogger implements Log, Serializable {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JLogger = null;
    static Class class$org$apache$log4j$Level = null;
    static Class class$org$apache$log4j$Priority = null;
    private static final long serialVersionUID = 5160705895411730424L;
    private static final Priority traceLevel;
    private volatile transient Logger logger;
    private final String name;

    static {
        Class class$;
        Class class$2;
        if (class$org$apache$commons$logging$impl$Log4JLogger == null) {
            class$ = class$("org.apache.commons.logging.impl.Log4JLogger");
            class$org$apache$commons$logging$impl$Log4JLogger = class$;
        } else {
            class$ = class$org$apache$commons$logging$impl$Log4JLogger;
        }
        FQCN = class$.getName();
        if (class$org$apache$log4j$Priority == null) {
            class$ = class$("org.apache.log4j.Priority");
            class$org$apache$log4j$Priority = class$;
        } else {
            class$ = class$org$apache$log4j$Priority;
        }
        if (class$org$apache$log4j$Level == null) {
            class$2 = class$("org.apache.log4j.Level");
            class$org$apache$log4j$Level = class$2;
        } else {
            class$2 = class$org$apache$log4j$Level;
        }
        if (class$.isAssignableFrom(class$2)) {
            Priority _traceLevel;
            try {
                if (class$org$apache$log4j$Level == null) {
                    class$ = class$("org.apache.log4j.Level");
                    class$org$apache$log4j$Level = class$;
                } else {
                    class$ = class$org$apache$log4j$Level;
                }
                _traceLevel = (Priority) class$.getDeclaredField(HttpTrace.METHOD_NAME).get(null);
            } catch (Exception e) {
                _traceLevel = Level.DEBUG;
            }
            traceLevel = _traceLevel;
            return;
        }
        throw new InstantiationError("Log4J 1.2 not available");
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public Log4JLogger() {
        this.logger = null;
        this.name = null;
    }

    public Log4JLogger(String name) {
        this.logger = null;
        this.name = name;
        this.logger = getLogger();
    }

    public Log4JLogger(Logger logger) {
        this.logger = null;
        if (logger == null) {
            throw new IllegalArgumentException("Warning - null logger in constructor; possible log4j misconfiguration.");
        }
        this.name = logger.getName();
        this.logger = logger;
    }

    public void trace(Object message) {
        getLogger().log(FQCN, traceLevel, message, null);
    }

    public void trace(Object message, Throwable t) {
        getLogger().log(FQCN, traceLevel, message, t);
    }

    public void debug(Object message) {
        getLogger().log(FQCN, Level.DEBUG, message, null);
    }

    public void debug(Object message, Throwable t) {
        getLogger().log(FQCN, Level.DEBUG, message, t);
    }

    public void info(Object message) {
        getLogger().log(FQCN, Level.INFO, message, null);
    }

    public void info(Object message, Throwable t) {
        getLogger().log(FQCN, Level.INFO, message, t);
    }

    public void warn(Object message) {
        getLogger().log(FQCN, Level.WARN, message, null);
    }

    public void warn(Object message, Throwable t) {
        getLogger().log(FQCN, Level.WARN, message, t);
    }

    public void error(Object message) {
        getLogger().log(FQCN, Level.ERROR, message, null);
    }

    public void error(Object message, Throwable t) {
        getLogger().log(FQCN, Level.ERROR, message, t);
    }

    public void fatal(Object message) {
        getLogger().log(FQCN, Level.FATAL, message, null);
    }

    public void fatal(Object message, Throwable t) {
        getLogger().log(FQCN, Level.FATAL, message, t);
    }

    public Logger getLogger() {
        Logger result = this.logger;
        if (result == null) {
            synchronized (this) {
                result = this.logger;
                if (result == null) {
                    result = Logger.getLogger(this.name);
                    this.logger = result;
                }
            }
        }
        return result;
    }

    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return getLogger().isEnabledFor(Level.ERROR);
    }

    public boolean isFatalEnabled() {
        return getLogger().isEnabledFor(Level.FATAL);
    }

    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return getLogger().isEnabledFor(traceLevel);
    }

    public boolean isWarnEnabled() {
        return getLogger().isEnabledFor(Level.WARN);
    }
}
