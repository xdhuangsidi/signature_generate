package org.apache.log4j.helpers;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.http.message.TokenParser;

public class DateTimeDateFormat extends AbsoluteTimeDateFormat {
    private static final long serialVersionUID = 5547637772208514971L;
    String[] shortMonths;

    public DateTimeDateFormat() {
        this.shortMonths = new DateFormatSymbols().getShortMonths();
    }

    public DateTimeDateFormat(TimeZone timeZone) {
        this();
        setCalendar(Calendar.getInstance(timeZone));
    }

    public StringBuffer format(Date date, StringBuffer sbuf, FieldPosition fieldPosition) {
        this.calendar.setTime(date);
        int day = this.calendar.get(5);
        if (day < 10) {
            sbuf.append('0');
        }
        sbuf.append(day);
        sbuf.append(TokenParser.SP);
        sbuf.append(this.shortMonths[this.calendar.get(2)]);
        sbuf.append(TokenParser.SP);
        sbuf.append(this.calendar.get(1));
        sbuf.append(TokenParser.SP);
        return super.format(date, sbuf, fieldPosition);
    }

    public Date parse(String s, ParsePosition pos) {
        return null;
    }
}
