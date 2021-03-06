package org.hamcrest.core;

import org.apache.log4j.spi.Configurator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class IsNull<T> extends BaseMatcher<T> {
    public boolean matches(Object o) {
        return o == null;
    }

    public void describeTo(Description description) {
        description.appendText(Configurator.NULL);
    }

    @Factory
    public static Matcher<Object> nullValue() {
        return new IsNull();
    }

    @Factory
    public static Matcher<Object> notNullValue() {
        return IsNot.not(nullValue());
    }

    @Factory
    public static <T> Matcher<T> nullValue(Class<T> cls) {
        return new IsNull();
    }

    @Factory
    public static <T> Matcher<T> notNullValue(Class<T> type) {
        return IsNot.not(nullValue(type));
    }
}
