package org.json.zip;

import org.apache.log4j.lf5.util.StreamUtils;

public abstract class JSONzip implements None, PostMortem {
    public static final byte[] bcd = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 46, (byte) 45, (byte) 43, (byte) 69};
    public static final int end = 256;
    public static final int endOfNumber = bcd.length;
    public static final long int14 = 16384;
    public static final long int4 = 16;
    public static final long int7 = 128;
    public static final int maxSubstringLength = 10;
    public static final int minSubstringLength = 3;
    public static final boolean probe = false;
    public static final int substringLimit = 40;
    public static final int[] twos = new int[]{1, 2, 4, 8, 16, 32, 64, 128, end, 512, 1024, StreamUtils.DEFAULT_BUFFER_SIZE, 4096, 8192, 16384, 32768, 65536};
    public static final int zipArrayString = 6;
    public static final int zipArrayValue = 7;
    public static final int zipEmptyArray = 1;
    public static final int zipEmptyObject = 0;
    public static final int zipFalse = 3;
    public static final int zipNull = 4;
    public static final int zipObject = 5;
    public static final int zipTrue = 2;
    protected final Huff namehuff = new Huff(257);
    protected final MapKeep namekeep = new MapKeep(9);
    protected final MapKeep stringkeep = new MapKeep(11);
    protected final Huff substringhuff = new Huff(257);
    protected final TrieKeep substringkeep = new TrieKeep(12);
    protected final MapKeep values = new MapKeep(10);

    protected JSONzip() {
        this.namehuff.tick(32, 125);
        this.namehuff.tick(97, 122);
        this.namehuff.tick(end);
        this.namehuff.tick(end);
        this.substringhuff.tick(32, 125);
        this.substringhuff.tick(97, 122);
        this.substringhuff.tick(end);
        this.substringhuff.tick(end);
    }

    protected void begin() {
        this.namehuff.generate();
        this.substringhuff.generate();
    }

    static void log() {
        log("\n");
    }

    static void log(int integer) {
        log(new StringBuffer().append(integer).append(" ").toString());
    }

    static void log(int integer, int width) {
        log(new StringBuffer().append(integer).append(":").append(width).append(" ").toString());
    }

    static void log(String string) {
        System.out.print(string);
    }

    static void logchar(int integer, int width) {
        if (integer <= 32 || integer > 125) {
            log(integer, width);
        } else {
            log(new StringBuffer().append("'").append((char) integer).append("':").append(width).append(" ").toString());
        }
    }

    public boolean postMortem(PostMortem pm) {
        JSONzip that = (JSONzip) pm;
        return this.namehuff.postMortem(that.namehuff) && this.namekeep.postMortem(that.namekeep) && this.stringkeep.postMortem(that.stringkeep) && this.substringhuff.postMortem(that.substringhuff) && this.substringkeep.postMortem(that.substringkeep) && this.values.postMortem(that.values);
    }
}
