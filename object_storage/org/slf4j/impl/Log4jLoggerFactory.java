package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Log4jLoggerFactory implements ILoggerFactory {
    Map loggerMap = new HashMap();

    public Logger getLogger(String name) {
        Throwable th;
        synchronized (this) {
            try {
                Logger slf4jLogger = (Logger) this.loggerMap.get(name);
                if (slf4jLogger == null) {
                    org.apache.log4j.Logger log4jLogger;
                    if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
                        log4jLogger = LogManager.getRootLogger();
                    } else {
                        log4jLogger = LogManager.getLogger(name);
                    }
                    Logger slf4jLogger2 = new Log4jLoggerAdapter(log4jLogger);
                    try {
                        this.loggerMap.put(name, slf4jLogger2);
                        slf4jLogger = slf4jLogger2;
                    } catch (Throwable th2) {
                        th = th2;
                        slf4jLogger = slf4jLogger2;
                        throw th;
                    }
                }
                return slf4jLogger;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }
}
