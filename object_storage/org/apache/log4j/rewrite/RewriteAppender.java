package org.apache.log4j.rewrite;

import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.xml.UnrecognizedElementHandler;
import org.w3c.dom.Element;

public class RewriteAppender extends AppenderSkeleton implements AppenderAttachable, UnrecognizedElementHandler {
    static Class class$org$apache$log4j$rewrite$RewritePolicy;
    private final AppenderAttachableImpl appenders = new AppenderAttachableImpl();
    private RewritePolicy policy;

    protected void append(LoggingEvent event) {
        LoggingEvent rewritten = event;
        if (this.policy != null) {
            rewritten = this.policy.rewrite(event);
        }
        if (rewritten != null) {
            synchronized (this.appenders) {
                this.appenders.appendLoopOnAppenders(rewritten);
            }
        }
    }

    public void addAppender(Appender newAppender) {
        synchronized (this.appenders) {
            this.appenders.addAppender(newAppender);
        }
    }

    public Enumeration getAllAppenders() {
        Enumeration allAppenders;
        synchronized (this.appenders) {
            allAppenders = this.appenders.getAllAppenders();
        }
        return allAppenders;
    }

    public Appender getAppender(String name) {
        Appender appender;
        synchronized (this.appenders) {
            appender = this.appenders.getAppender(name);
        }
        return appender;
    }

    public void close() {
        this.closed = true;
        synchronized (this.appenders) {
            Enumeration iter = this.appenders.getAllAppenders();
            if (iter != null) {
                while (iter.hasMoreElements()) {
                    Object next = iter.nextElement();
                    if (next instanceof Appender) {
                        ((Appender) next).close();
                    }
                }
            }
        }
    }

    public boolean isAttached(Appender appender) {
        boolean isAttached;
        synchronized (this.appenders) {
            isAttached = this.appenders.isAttached(appender);
        }
        return isAttached;
    }

    public boolean requiresLayout() {
        return false;
    }

    public void removeAllAppenders() {
        synchronized (this.appenders) {
            this.appenders.removeAllAppenders();
        }
    }

    public void removeAppender(Appender appender) {
        synchronized (this.appenders) {
            this.appenders.removeAppender(appender);
        }
    }

    public void removeAppender(String name) {
        synchronized (this.appenders) {
            this.appenders.removeAppender(name);
        }
    }

    public void setRewritePolicy(RewritePolicy rewritePolicy) {
        this.policy = rewritePolicy;
    }

    public boolean parseUnrecognizedElement(Element element, Properties props) throws Exception {
        if (!"rewritePolicy".equals(element.getNodeName())) {
            return false;
        }
        Class class$;
        if (class$org$apache$log4j$rewrite$RewritePolicy == null) {
            class$ = class$("org.apache.log4j.rewrite.RewritePolicy");
            class$org$apache$log4j$rewrite$RewritePolicy = class$;
        } else {
            class$ = class$org$apache$log4j$rewrite$RewritePolicy;
        }
        Object rewritePolicy = DOMConfigurator.parseElement(element, props, class$);
        if (rewritePolicy != null) {
            if (rewritePolicy instanceof OptionHandler) {
                ((OptionHandler) rewritePolicy).activateOptions();
            }
            setRewritePolicy((RewritePolicy) rewritePolicy);
        }
        return true;
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError().initCause(x1);
        }
    }
}
