package org.apache.http.protocol;

import org.apache.http.HttpRequest;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.util.Args;
import org.apache.log4j.spi.LocationInfo;

@ThreadSafe
public class UriHttpRequestHandlerMapper implements HttpRequestHandlerMapper {
    private final UriPatternMatcher<HttpRequestHandler> matcher;

    protected UriHttpRequestHandlerMapper(UriPatternMatcher<HttpRequestHandler> matcher) {
        this.matcher = (UriPatternMatcher) Args.notNull(matcher, "Pattern matcher");
    }

    public UriHttpRequestHandlerMapper() {
        this(new UriPatternMatcher());
    }

    public void register(String pattern, HttpRequestHandler handler) {
        Args.notNull(pattern, "Pattern");
        Args.notNull(handler, "Handler");
        this.matcher.register(pattern, handler);
    }

    public void unregister(String pattern) {
        this.matcher.unregister(pattern);
    }

    protected String getRequestPath(HttpRequest request) {
        String uriPath = request.getRequestLine().getUri();
        int index = uriPath.indexOf(LocationInfo.NA);
        if (index != -1) {
            return uriPath.substring(0, index);
        }
        index = uriPath.indexOf("#");
        if (index != -1) {
            return uriPath.substring(0, index);
        }
        return uriPath;
    }

    public HttpRequestHandler lookup(HttpRequest request) {
        Args.notNull(request, "HTTP request");
        return (HttpRequestHandler) this.matcher.lookup(getRequestPath(request));
    }
}
