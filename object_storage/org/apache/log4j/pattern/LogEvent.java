package org.apache.log4j.pattern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.spi.ThrowableInformation;

public class LogEvent implements Serializable {
    static final Integer[] PARAM_ARRAY = new Integer[1];
    static final String TO_LEVEL = "toLevel";
    static final Class[] TO_LEVEL_PARAMS = new Class[]{Integer.TYPE};
    static Class class$org$apache$log4j$Level = null;
    static final Hashtable methodCache = new Hashtable(3);
    static final long serialVersionUID = -868428216207166145L;
    private static long startTime = System.currentTimeMillis();
    public final String categoryName;
    public final transient String fqnOfCategoryClass;
    public transient Priority level;
    private LocationInfo locationInfo;
    private transient Category logger;
    private Hashtable mdcCopy;
    private boolean mdcCopyLookupRequired = true;
    private transient Object message;
    private String ndc;
    private boolean ndcLookupRequired = true;
    private String renderedMessage;
    private String threadName;
    private ThrowableInformation throwableInfo;
    public final long timeStamp;

    public LogEvent(String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable throwable) {
        this.fqnOfCategoryClass = fqnOfCategoryClass;
        this.logger = logger;
        this.categoryName = logger.getName();
        this.level = level;
        this.message = message;
        if (throwable != null) {
            this.throwableInfo = new ThrowableInformation(throwable);
        }
        this.timeStamp = System.currentTimeMillis();
    }

    public LogEvent(String fqnOfCategoryClass, Category logger, long timeStamp, Priority level, Object message, Throwable throwable) {
        this.fqnOfCategoryClass = fqnOfCategoryClass;
        this.logger = logger;
        this.categoryName = logger.getName();
        this.level = level;
        this.message = message;
        if (throwable != null) {
            this.throwableInfo = new ThrowableInformation(throwable);
        }
        this.timeStamp = timeStamp;
    }

    public LogEvent(String fqnOfCategoryClass, Logger logger, long timeStamp, Level level, Object message, String threadName, ThrowableInformation throwable, String ndc, LocationInfo info, Map properties) {
        this.fqnOfCategoryClass = fqnOfCategoryClass;
        this.logger = logger;
        if (logger != null) {
            this.categoryName = logger.getName();
        } else {
            this.categoryName = null;
        }
        this.level = level;
        this.message = message;
        if (throwable != null) {
            this.throwableInfo = throwable;
        }
        this.timeStamp = timeStamp;
        this.threadName = threadName;
        this.ndcLookupRequired = false;
        this.ndc = ndc;
        this.locationInfo = info;
        this.mdcCopyLookupRequired = false;
        if (properties != null) {
            this.mdcCopy = new Hashtable(properties);
        }
    }

    public LocationInfo getLocationInformation() {
        if (this.locationInfo == null) {
            this.locationInfo = new LocationInfo(new Throwable(), this.fqnOfCategoryClass);
        }
        return this.locationInfo;
    }

    public Level getLevel() {
        return (Level) this.level;
    }

    public String getLoggerName() {
        return this.categoryName;
    }

    public Object getMessage() {
        if (this.message != null) {
            return this.message;
        }
        return getRenderedMessage();
    }

    public String getNDC() {
        if (this.ndcLookupRequired) {
            this.ndcLookupRequired = false;
            this.ndc = NDC.get();
        }
        return this.ndc;
    }

    public Object getMDC(String key) {
        if (this.mdcCopy != null) {
            Object r = this.mdcCopy.get(key);
            if (r != null) {
                return r;
            }
        }
        return MDC.get(key);
    }

    public void getMDCCopy() {
        if (this.mdcCopyLookupRequired) {
            this.mdcCopyLookupRequired = false;
            Hashtable t = MDC.getContext();
            if (t != null) {
                this.mdcCopy = (Hashtable) t.clone();
            }
        }
    }

    public String getRenderedMessage() {
        if (this.renderedMessage == null && this.message != null) {
            if (this.message instanceof String) {
                this.renderedMessage = (String) this.message;
            } else {
                LoggerRepository repository = this.logger.getLoggerRepository();
                if (repository instanceof RendererSupport) {
                    this.renderedMessage = ((RendererSupport) repository).getRendererMap().findAndRender(this.message);
                } else {
                    this.renderedMessage = this.message.toString();
                }
            }
        }
        return this.renderedMessage;
    }

    public static long getStartTime() {
        return startTime;
    }

    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }
        return this.threadName;
    }

    public ThrowableInformation getThrowableInformation() {
        return this.throwableInfo;
    }

    public String[] getThrowableStrRep() {
        if (this.throwableInfo == null) {
            return null;
        }
        return this.throwableInfo.getThrowableStrRep();
    }

    private void readLevel(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int p = ois.readInt();
        try {
            String className = (String) ois.readObject();
            if (className == null) {
                this.level = Level.toLevel(p);
                return;
            }
            Method m = (Method) methodCache.get(className);
            if (m == null) {
                m = Loader.loadClass(className).getDeclaredMethod(TO_LEVEL, TO_LEVEL_PARAMS);
                methodCache.put(className, m);
            }
            PARAM_ARRAY[0] = new Integer(p);
            this.level = (Level) m.invoke(null, PARAM_ARRAY);
        } catch (Exception e) {
            LogLog.warn("Level deserialization failed, reverting to default.", e);
            this.level = Level.toLevel(p);
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        readLevel(ois);
        if (this.locationInfo == null) {
            this.locationInfo = new LocationInfo(null, null);
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        getThreadName();
        getRenderedMessage();
        getNDC();
        getMDCCopy();
        getThrowableStrRep();
        oos.defaultWriteObject();
        writeLevel(oos);
    }

    private void writeLevel(ObjectOutputStream oos) throws IOException {
        Class class$;
        oos.writeInt(this.level.toInt());
        Class clazz = this.level.getClass();
        if (class$org$apache$log4j$Level == null) {
            class$ = class$("org.apache.log4j.Level");
            class$org$apache$log4j$Level = class$;
        } else {
            class$ = class$org$apache$log4j$Level;
        }
        if (clazz == class$) {
            oos.writeObject(null);
        } else {
            oos.writeObject(clazz.getName());
        }
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError().initCause(x1);
        }
    }

    public final void setProperty(String propName, String propValue) {
        if (this.mdcCopy == null) {
            getMDCCopy();
        }
        if (this.mdcCopy == null) {
            this.mdcCopy = new Hashtable();
        }
        this.mdcCopy.put(propName, propValue);
    }

    public final String getProperty(String key) {
        Object value = getMDC(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public final boolean locationInformationExists() {
        return this.locationInfo != null;
    }

    public final long getTimeStamp() {
        return this.timeStamp;
    }

    public Set getPropertyKeySet() {
        return getProperties().keySet();
    }

    public Map getProperties() {
        Map properties;
        getMDCCopy();
        if (this.mdcCopy == null) {
            properties = new HashMap();
        } else {
            properties = this.mdcCopy;
        }
        return Collections.unmodifiableMap(properties);
    }

    public String getFQNOfLoggerClass() {
        return this.fqnOfCategoryClass;
    }
}
