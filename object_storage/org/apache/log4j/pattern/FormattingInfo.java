package org.apache.log4j.pattern;

import org.apache.http.message.TokenParser;
import org.apache.log4j.Priority;

public final class FormattingInfo {
    private static final FormattingInfo DEFAULT = new FormattingInfo(false, 0, Priority.OFF_INT);
    private static final char[] SPACES = new char[]{TokenParser.SP, TokenParser.SP, TokenParser.SP, TokenParser.SP, TokenParser.SP, TokenParser.SP, TokenParser.SP, TokenParser.SP};
    private final boolean leftAlign;
    private final int maxLength;
    private final int minLength;

    public FormattingInfo(boolean leftAlign, int minLength, int maxLength) {
        this.leftAlign = leftAlign;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public static FormattingInfo getDefault() {
        return DEFAULT;
    }

    public boolean isLeftAligned() {
        return this.leftAlign;
    }

    public int getMinLength() {
        return this.minLength;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void format(int fieldStart, StringBuffer buffer) {
        int rawLength = buffer.length() - fieldStart;
        if (rawLength > this.maxLength) {
            buffer.delete(fieldStart, buffer.length() - this.maxLength);
        } else if (rawLength >= this.minLength) {
        } else {
            if (this.leftAlign) {
                int fieldEnd = buffer.length();
                buffer.setLength(this.minLength + fieldStart);
                for (int i = fieldEnd; i < buffer.length(); i++) {
                    buffer.setCharAt(i, TokenParser.SP);
                }
                return;
            }
            int padLength = this.minLength - rawLength;
            while (padLength > 8) {
                buffer.insert(fieldStart, SPACES);
                padLength -= 8;
            }
            buffer.insert(fieldStart, SPACES, 0, padLength);
        }
    }
}
