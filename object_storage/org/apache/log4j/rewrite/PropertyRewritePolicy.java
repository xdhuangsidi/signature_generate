package org.apache.log4j.rewrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class PropertyRewritePolicy implements RewritePolicy {
    private Map properties = Collections.EMPTY_MAP;

    public void setProperties(String props) {
        Map hashTable = new HashMap();
        StringTokenizer pairs = new StringTokenizer(props, ",");
        while (pairs.hasMoreTokens()) {
            StringTokenizer entry = new StringTokenizer(pairs.nextToken(), "=");
            hashTable.put(entry.nextElement().toString().trim(), entry.nextElement().toString().trim());
        }
        synchronized (this) {
            this.properties = hashTable;
        }
    }

    public LoggingEvent rewrite(LoggingEvent source) {
        if (this.properties.isEmpty()) {
            return source;
        }
        Category logger;
        Map rewriteProps = new HashMap(source.getProperties());
        for (Entry entry : this.properties.entrySet()) {
            if (!rewriteProps.containsKey(entry.getKey())) {
                rewriteProps.put(entry.getKey(), entry.getValue());
            }
        }
        String fQNOfLoggerClass = source.getFQNOfLoggerClass();
        if (source.getLogger() != null) {
            logger = source.getLogger();
        } else {
            logger = Logger.getLogger(source.getLoggerName());
        }
        return new LoggingEvent(fQNOfLoggerClass, logger, source.getTimeStamp(), source.getLevel(), source.getMessage(), source.getThreadName(), source.getThrowableInformation(), source.getNDC(), source.getLocationInformation(), rewriteProps);
    }
}
