package org.hamcrest;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.http.message.TokenParser;
import org.apache.log4j.spi.Configurator;
import org.hamcrest.internal.ArrayIterator;
import org.hamcrest.internal.SelfDescribingValueIterator;

public abstract class BaseDescription implements Description {
    protected abstract void append(char c);

    public Description appendText(String text) {
        append(text);
        return this;
    }

    public Description appendDescriptionOf(SelfDescribing value) {
        value.describeTo(this);
        return this;
    }

    public Description appendValue(Object value) {
        if (value == null) {
            append(Configurator.NULL);
        } else if (value instanceof String) {
            toJavaSyntax((String) value);
        } else if (value instanceof Character) {
            append((char) TokenParser.DQUOTE);
            toJavaSyntax(((Character) value).charValue());
            append((char) TokenParser.DQUOTE);
        } else if (value instanceof Short) {
            append('<');
            append(descriptionOf(value));
            append("s>");
        } else if (value instanceof Long) {
            append('<');
            append(descriptionOf(value));
            append("L>");
        } else if (value instanceof Float) {
            append('<');
            append(descriptionOf(value));
            append("F>");
        } else if (value.getClass().isArray()) {
            appendValueList("[", ", ", "]", new ArrayIterator(value));
        } else {
            append('<');
            append(descriptionOf(value));
            append('>');
        }
        return this;
    }

    private String descriptionOf(Object value) {
        try {
            return String.valueOf(value);
        } catch (Exception e) {
            return value.getClass().getName() + "@" + Integer.toHexString(value.hashCode());
        }
    }

    public <T> Description appendValueList(String start, String separator, String end, T... values) {
        return appendValueList(start, separator, end, Arrays.asList(values));
    }

    public <T> Description appendValueList(String start, String separator, String end, Iterable<T> values) {
        return appendValueList(start, separator, end, values.iterator());
    }

    private <T> Description appendValueList(String start, String separator, String end, Iterator<T> values) {
        return appendList(start, separator, end, new SelfDescribingValueIterator(values));
    }

    public Description appendList(String start, String separator, String end, Iterable<? extends SelfDescribing> values) {
        return appendList(start, separator, end, values.iterator());
    }

    private Description appendList(String start, String separator, String end, Iterator<? extends SelfDescribing> i) {
        boolean separate = false;
        append(start);
        while (i.hasNext()) {
            if (separate) {
                append(separator);
            }
            appendDescriptionOf((SelfDescribing) i.next());
            separate = true;
        }
        append(end);
        return this;
    }

    protected void append(String str) {
        for (int i = 0; i < str.length(); i++) {
            append(str.charAt(i));
        }
    }

    private void toJavaSyntax(String unformatted) {
        append((char) TokenParser.DQUOTE);
        for (int i = 0; i < unformatted.length(); i++) {
            toJavaSyntax(unformatted.charAt(i));
        }
        append((char) TokenParser.DQUOTE);
    }

    private void toJavaSyntax(char ch) {
        switch (ch) {
            case '\t':
                append("\\t");
                return;
            case '\n':
                append("\\n");
                return;
            case '\r':
                append("\\r");
                return;
            case '\"':
                append("\\\"");
                return;
            default:
                append(ch);
                return;
        }
    }
}
