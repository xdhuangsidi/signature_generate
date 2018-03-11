package org.json;

import org.apache.http.message.TokenParser;

public class HTTPTokener extends JSONTokener {
    public HTTPTokener(String string) {
        super(string);
    }

    public String nextToken() throws JSONException {
        char c;
        StringBuffer sb = new StringBuffer();
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if (c == TokenParser.DQUOTE || c == '\'') {
            char q = c;
            while (true) {
                c = next();
                if (c < TokenParser.SP) {
                    throw syntaxError("Unterminated string.");
                } else if (c == q) {
                    return sb.toString();
                } else {
                    sb.append(c);
                }
            }
        } else {
            while (c != '\u0000' && !Character.isWhitespace(c)) {
                sb.append(c);
                c = next();
            }
            return sb.toString();
        }
    }
}
