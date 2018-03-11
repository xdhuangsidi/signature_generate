package org.apache.log4j.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.http.message.TokenParser;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.SyslogQuietWriter;
import org.apache.log4j.helpers.SyslogWriter;
import org.apache.log4j.spi.LoggingEvent;
import org.json.zip.JSONzip;

public class SyslogAppender extends AppenderSkeleton {
    protected static final int FACILITY_OI = 1;
    public static final int LOG_AUTH = 32;
    public static final int LOG_AUTHPRIV = 80;
    public static final int LOG_CRON = 72;
    public static final int LOG_DAEMON = 24;
    public static final int LOG_FTP = 88;
    public static final int LOG_KERN = 0;
    public static final int LOG_LOCAL0 = 128;
    public static final int LOG_LOCAL1 = 136;
    public static final int LOG_LOCAL2 = 144;
    public static final int LOG_LOCAL3 = 152;
    public static final int LOG_LOCAL4 = 160;
    public static final int LOG_LOCAL5 = 168;
    public static final int LOG_LOCAL6 = 176;
    public static final int LOG_LOCAL7 = 184;
    public static final int LOG_LPR = 48;
    public static final int LOG_MAIL = 16;
    public static final int LOG_NEWS = 56;
    public static final int LOG_SYSLOG = 40;
    public static final int LOG_USER = 8;
    public static final int LOG_UUCP = 64;
    protected static final int SYSLOG_HOST_OI = 0;
    static final String TAB = "    ";
    private final SimpleDateFormat dateFormat;
    boolean facilityPrinting;
    String facilityStr;
    private boolean header;
    private boolean layoutHeaderChecked;
    private String localHostname;
    SyslogQuietWriter sqw;
    int syslogFacility;
    String syslogHost;

    public SyslogAppender() {
        this.syslogFacility = 8;
        this.facilityPrinting = false;
        this.header = false;
        this.dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss ", Locale.ENGLISH);
        this.layoutHeaderChecked = false;
        initSyslogFacilityStr();
    }

    public SyslogAppender(Layout layout, int syslogFacility) {
        this.syslogFacility = 8;
        this.facilityPrinting = false;
        this.header = false;
        this.dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss ", Locale.ENGLISH);
        this.layoutHeaderChecked = false;
        this.layout = layout;
        this.syslogFacility = syslogFacility;
        initSyslogFacilityStr();
    }

    public SyslogAppender(Layout layout, String syslogHost, int syslogFacility) {
        this(layout, syslogFacility);
        setSyslogHost(syslogHost);
    }

    public synchronized void close() {
        this.closed = true;
        if (this.sqw != null) {
            try {
                if (!(!this.layoutHeaderChecked || this.layout == null || this.layout.getFooter() == null)) {
                    sendLayoutMessage(this.layout.getFooter());
                }
                this.sqw.close();
                this.sqw = null;
            } catch (InterruptedIOException e) {
                Thread.currentThread().interrupt();
                this.sqw = null;
            } catch (IOException e2) {
                this.sqw = null;
            }
        }
    }

    private void initSyslogFacilityStr() {
        this.facilityStr = getFacilityString(this.syslogFacility);
        if (this.facilityStr == null) {
            System.err.println(new StringBuffer().append("\"").append(this.syslogFacility).append("\" is an unknown syslog facility. Defaulting to \"USER\".").toString());
            this.syslogFacility = 8;
            this.facilityStr = "user:";
            return;
        }
        this.facilityStr = new StringBuffer().append(this.facilityStr).append(":").toString();
    }

    public static String getFacilityString(int syslogFacility) {
        switch (syslogFacility) {
            case 0:
                return "kern";
            case LOG_USER /*8*/:
                return "user";
            case LOG_MAIL /*16*/:
                return "mail";
            case LOG_DAEMON /*24*/:
                return "daemon";
            case 32:
                return "auth";
            case 40:
                return "syslog";
            case LOG_LPR /*48*/:
                return "lpr";
            case LOG_NEWS /*56*/:
                return "news";
            case 64:
                return "uucp";
            case LOG_CRON /*72*/:
                return "cron";
            case LOG_AUTHPRIV /*80*/:
                return "authpriv";
            case LOG_FTP /*88*/:
                return "ftp";
            case 128:
                return "local0";
            case LOG_LOCAL1 /*136*/:
                return "local1";
            case LOG_LOCAL2 /*144*/:
                return "local2";
            case LOG_LOCAL3 /*152*/:
                return "local3";
            case LOG_LOCAL4 /*160*/:
                return "local4";
            case LOG_LOCAL5 /*168*/:
                return "local5";
            case LOG_LOCAL6 /*176*/:
                return "local6";
            case LOG_LOCAL7 /*184*/:
                return "local7";
            default:
                return null;
        }
    }

    public static int getFacility(String facilityName) {
        if (facilityName != null) {
            facilityName = facilityName.trim();
        }
        if ("KERN".equalsIgnoreCase(facilityName)) {
            return 0;
        }
        if ("USER".equalsIgnoreCase(facilityName)) {
            return 8;
        }
        if ("MAIL".equalsIgnoreCase(facilityName)) {
            return 16;
        }
        if ("DAEMON".equalsIgnoreCase(facilityName)) {
            return 24;
        }
        if ("AUTH".equalsIgnoreCase(facilityName)) {
            return 32;
        }
        if ("SYSLOG".equalsIgnoreCase(facilityName)) {
            return 40;
        }
        if ("LPR".equalsIgnoreCase(facilityName)) {
            return 48;
        }
        if ("NEWS".equalsIgnoreCase(facilityName)) {
            return 56;
        }
        if ("UUCP".equalsIgnoreCase(facilityName)) {
            return 64;
        }
        if ("CRON".equalsIgnoreCase(facilityName)) {
            return 72;
        }
        if ("AUTHPRIV".equalsIgnoreCase(facilityName)) {
            return 80;
        }
        if ("FTP".equalsIgnoreCase(facilityName)) {
            return 88;
        }
        if ("LOCAL0".equalsIgnoreCase(facilityName)) {
            return 128;
        }
        if ("LOCAL1".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL1;
        }
        if ("LOCAL2".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL2;
        }
        if ("LOCAL3".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL3;
        }
        if ("LOCAL4".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL4;
        }
        if ("LOCAL5".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL5;
        }
        if ("LOCAL6".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL6;
        }
        if ("LOCAL7".equalsIgnoreCase(facilityName)) {
            return LOG_LOCAL7;
        }
        return -1;
    }

    private void splitPacket(String header, String packet) {
        if (packet.getBytes().length <= 1019) {
            this.sqw.write(packet);
            return;
        }
        int split = header.length() + ((packet.length() - header.length()) / 2);
        splitPacket(header, new StringBuffer().append(packet.substring(0, split)).append("...").toString());
        splitPacket(header, new StringBuffer().append(header).append("...").append(packet.substring(split)).toString());
    }

    public void append(LoggingEvent event) {
        if (!isAsSevereAsThreshold(event.getLevel())) {
            return;
        }
        if (this.sqw == null) {
            this.errorHandler.error(new StringBuffer().append("No syslog host is set for SyslogAppedender named \"").append(this.name).append("\".").toString());
            return;
        }
        String packet;
        if (!this.layoutHeaderChecked) {
            if (!(this.layout == null || this.layout.getHeader() == null)) {
                sendLayoutMessage(this.layout.getHeader());
            }
            this.layoutHeaderChecked = true;
        }
        String hdr = getPacketHeader(event.timeStamp);
        if (this.layout == null) {
            packet = String.valueOf(event.getMessage());
        } else {
            packet = this.layout.format(event);
        }
        if (this.facilityPrinting || hdr.length() > 0) {
            StringBuffer buf = new StringBuffer(hdr);
            if (this.facilityPrinting) {
                buf.append(this.facilityStr);
            }
            buf.append(packet);
            packet = buf.toString();
        }
        this.sqw.setLevel(event.getLevel().getSyslogEquivalent());
        if (packet.length() > JSONzip.end) {
            splitPacket(hdr, packet);
        } else {
            this.sqw.write(packet);
        }
        if (this.layout == null || this.layout.ignoresThrowable()) {
            String[] s = event.getThrowableStrRep();
            if (s != null) {
                for (int i = 0; i < s.length; i++) {
                    if (s[i].startsWith("\t")) {
                        this.sqw.write(new StringBuffer().append(hdr).append(TAB).append(s[i].substring(1)).toString());
                    } else {
                        this.sqw.write(new StringBuffer().append(hdr).append(s[i]).toString());
                    }
                }
            }
        }
    }

    public void activateOptions() {
        if (this.header) {
            getLocalHostname();
        }
        if (!(this.layout == null || this.layout.getHeader() == null)) {
            sendLayoutMessage(this.layout.getHeader());
        }
        this.layoutHeaderChecked = true;
    }

    public boolean requiresLayout() {
        return true;
    }

    public void setSyslogHost(String syslogHost) {
        this.sqw = new SyslogQuietWriter(new SyslogWriter(syslogHost), this.syslogFacility, this.errorHandler);
        this.syslogHost = syslogHost;
    }

    public String getSyslogHost() {
        return this.syslogHost;
    }

    public void setFacility(String facilityName) {
        if (facilityName != null) {
            this.syslogFacility = getFacility(facilityName);
            if (this.syslogFacility == -1) {
                System.err.println(new StringBuffer().append("[").append(facilityName).append("] is an unknown syslog facility. Defaulting to [USER].").toString());
                this.syslogFacility = 8;
            }
            initSyslogFacilityStr();
            if (this.sqw != null) {
                this.sqw.setSyslogFacility(this.syslogFacility);
            }
        }
    }

    public String getFacility() {
        return getFacilityString(this.syslogFacility);
    }

    public void setFacilityPrinting(boolean on) {
        this.facilityPrinting = on;
    }

    public boolean getFacilityPrinting() {
        return this.facilityPrinting;
    }

    public final boolean getHeader() {
        return this.header;
    }

    public final void setHeader(boolean val) {
        this.header = val;
    }

    private String getLocalHostname() {
        if (this.localHostname == null) {
            try {
                this.localHostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                this.localHostname = "UNKNOWN_HOST";
            }
        }
        return this.localHostname;
    }

    private String getPacketHeader(long timeStamp) {
        if (!this.header) {
            return "";
        }
        StringBuffer buf = new StringBuffer(this.dateFormat.format(new Date(timeStamp)));
        if (buf.charAt(4) == '0') {
            buf.setCharAt(4, TokenParser.SP);
        }
        buf.append(getLocalHostname());
        buf.append(TokenParser.SP);
        return buf.toString();
    }

    private void sendLayoutMessage(String msg) {
        if (this.sqw != null) {
            String packet = msg;
            String hdr = getPacketHeader(new Date().getTime());
            if (this.facilityPrinting || hdr.length() > 0) {
                StringBuffer buf = new StringBuffer(hdr);
                if (this.facilityPrinting) {
                    buf.append(this.facilityStr);
                }
                buf.append(msg);
                packet = buf.toString();
            }
            this.sqw.setLevel(6);
            this.sqw.write(packet);
        }
    }
}
