package org.apache.http.message;

import java.util.BitSet;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.CharArrayBuffer;

@Immutable
public class TokenParser {
    public static final char CR = '\r';
    public static final char DQUOTE = '\"';
    public static final char ESCAPE = '\\';
    public static final char HT = '\t';
    public static final TokenParser INSTANCE = new TokenParser();
    public static final char LF = '\n';
    public static final char SP = ' ';

    public static BitSet INIT_BITSET(int... b) {
        BitSet bitset = new BitSet();
        for (int aB : b) {
            bitset.set(aB);
        }
        return bitset;
    }

    public static boolean isWhitespace(char ch) {
        return ch == SP || ch == '\t' || ch == CR || ch == '\n';
    }

    public String parseToken(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = buf.charAt(cursor.getPos());
            if (delimiters != null && delimiters.get(current)) {
                break;
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                whitespace = true;
            } else {
                if (whitespace && dst.length() > 0) {
                    dst.append(SP);
                }
                copyContent(buf, cursor, delimiters, dst);
                whitespace = false;
            }
        }
        return dst.toString();
    }

    public String parseValue(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = buf.charAt(cursor.getPos());
            if (delimiters != null && delimiters.get(current)) {
                break;
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                whitespace = true;
            } else if (current == DQUOTE) {
                if (whitespace && dst.length() > 0) {
                    dst.append(SP);
                }
                copyQuotedContent(buf, cursor, dst);
                whitespace = false;
            } else {
                if (whitespace && dst.length() > 0) {
                    dst.append(SP);
                }
                copyUnquotedContent(buf, cursor, delimiters, dst);
                whitespace = false;
            }
        }
        return dst.toString();
    }

    public void skipWhiteSpace(CharArrayBuffer buf, ParserCursor cursor) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        int i = indexFrom;
        while (i < indexTo && isWhitespace(buf.charAt(i))) {
            pos++;
            i++;
        }
        cursor.updatePos(pos);
    }

    public void copyContent(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = buf.charAt(i);
            if ((delimiters != null && delimiters.get(current)) || isWhitespace(current)) {
                break;
            }
            pos++;
            dst.append(current);
        }
        cursor.updatePos(pos);
    }

    public void copyUnquotedContent(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = buf.charAt(i);
            if ((delimiters != null && delimiters.get(current)) || isWhitespace(current) || current == DQUOTE) {
                break;
            }
            pos++;
            dst.append(current);
        }
        cursor.updatePos(pos);
    }

    public void copyQuotedContent(CharArrayBuffer buf, ParserCursor cursor, StringBuilder dst) {
        if (!cursor.atEnd()) {
            int pos = cursor.getPos();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            if (buf.charAt(pos) == DQUOTE) {
                pos++;
                boolean escaped = false;
                int i = indexFrom + 1;
                while (i < indexTo) {
                    char current = buf.charAt(i);
                    if (escaped) {
                        if (!(current == DQUOTE || current == ESCAPE)) {
                            dst.append(ESCAPE);
                        }
                        dst.append(current);
                        escaped = false;
                    } else if (current == DQUOTE) {
                        pos++;
                        break;
                    } else if (current == ESCAPE) {
                        escaped = true;
                    } else if (!(current == CR || current == '\n')) {
                        dst.append(current);
                    }
                    i++;
                    pos++;
                }
                cursor.updatePos(pos);
            }
        }
    }
}
