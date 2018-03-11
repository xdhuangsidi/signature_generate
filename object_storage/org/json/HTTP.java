package org.json;

import java.util.Iterator;
import org.apache.http.HttpVersion;
import org.apache.http.message.TokenParser;

public class HTTP {
    public static final String CRLF = "\r\n";

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        HTTPTokener x = new HTTPTokener(string);
        Object token = x.nextToken();
        if (token.toUpperCase().startsWith(HttpVersion.HTTP)) {
            jo.put("HTTP-Version", token);
            jo.put("Status-Code", x.nextToken());
            jo.put("Reason-Phrase", x.nextTo('\u0000'));
            x.next();
        } else {
            jo.put("Method", token);
            jo.put("Request-URI", x.nextToken());
            jo.put("HTTP-Version", x.nextToken());
        }
        while (x.more()) {
            String name = x.nextTo(':');
            x.next(':');
            jo.put(name, x.nextTo('\u0000'));
            x.next();
        }
        return jo;
    }

    public static String toString(JSONObject jo) throws JSONException {
        Iterator keys = jo.keys();
        StringBuffer sb = new StringBuffer();
        if (jo.has("Status-Code") && jo.has("Reason-Phrase")) {
            sb.append(jo.getString("HTTP-Version"));
            sb.append(TokenParser.SP);
            sb.append(jo.getString("Status-Code"));
            sb.append(TokenParser.SP);
            sb.append(jo.getString("Reason-Phrase"));
        } else if (jo.has("Method") && jo.has("Request-URI")) {
            sb.append(jo.getString("Method"));
            sb.append(TokenParser.SP);
            sb.append(TokenParser.DQUOTE);
            sb.append(jo.getString("Request-URI"));
            sb.append(TokenParser.DQUOTE);
            sb.append(TokenParser.SP);
            sb.append(jo.getString("HTTP-Version"));
        } else {
            throw new JSONException("Not enough material for an HTTP header.");
        }
        sb.append(CRLF);
        while (keys.hasNext()) {
            String string = keys.next().toString();
            if (!("HTTP-Version".equals(string) || "Status-Code".equals(string) || "Reason-Phrase".equals(string) || "Method".equals(string) || "Request-URI".equals(string) || jo.isNull(string))) {
                sb.append(string);
                sb.append(": ");
                sb.append(jo.getString(string));
                sb.append(CRLF);
            }
        }
        sb.append(CRLF);
        return sb.toString();
    }
}
