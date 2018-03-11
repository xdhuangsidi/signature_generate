package org.json;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.message.TokenParser;

public class Cookie {
    public static String escape(String string) {
        String s = string.trim();
        StringBuffer sb = new StringBuffer();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c < TokenParser.SP || c == '+' || c == '%' || c == '=' || c == ';') {
                sb.append('%');
                sb.append(Character.forDigit((char) ((c >>> 4) & 15), 16));
                sb.append(Character.forDigit((char) (c & 15), 16));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONTokener x = new JSONTokener(string);
        jo.put("name", x.nextTo('='));
        x.next('=');
        jo.put("value", x.nextTo(';'));
        x.next();
        while (x.more()) {
            Object value;
            String name = unescape(x.nextTo("=;"));
            if (x.next() == '=') {
                value = unescape(x.nextTo(';'));
                x.next();
            } else if (name.equals(ClientCookie.SECURE_ATTR)) {
                value = Boolean.TRUE;
            } else {
                throw x.syntaxError("Missing '=' in cookie parameter.");
            }
            jo.put(name, value);
        }
        return jo;
    }

    public static String toString(JSONObject jo) throws JSONException {
        StringBuffer sb = new StringBuffer();
        sb.append(escape(jo.getString("name")));
        sb.append("=");
        sb.append(escape(jo.getString("value")));
        if (jo.has(ClientCookie.EXPIRES_ATTR)) {
            sb.append(";expires=");
            sb.append(jo.getString(ClientCookie.EXPIRES_ATTR));
        }
        if (jo.has(ClientCookie.DOMAIN_ATTR)) {
            sb.append(";domain=");
            sb.append(escape(jo.getString(ClientCookie.DOMAIN_ATTR)));
        }
        if (jo.has(ClientCookie.PATH_ATTR)) {
            sb.append(";path=");
            sb.append(escape(jo.getString(ClientCookie.PATH_ATTR)));
        }
        if (jo.optBoolean(ClientCookie.SECURE_ATTR)) {
            sb.append(";secure");
        }
        return sb.toString();
    }

    public static String unescape(String string) {
        int length = string.length();
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < length) {
            char c = string.charAt(i);
            if (c == '+') {
                c = TokenParser.SP;
            } else if (c == '%' && i + 2 < length) {
                int d = JSONTokener.dehexchar(string.charAt(i + 1));
                int e = JSONTokener.dehexchar(string.charAt(i + 2));
                if (d >= 0 && e >= 0) {
                    c = (char) ((d * 16) + e);
                    i += 2;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
}
