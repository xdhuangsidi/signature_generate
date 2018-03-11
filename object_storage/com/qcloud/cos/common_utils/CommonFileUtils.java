package com.qcloud.cos.common_utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonFileUtils {
    private static Logger LOG = LoggerFactory.getLogger(CommonFileUtils.class);

    public static boolean isLegalFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory() && file.canRead()) {
            return true;
        }
        return false;
    }

    public static long getFileLength(String filePath) throws Exception {
        return new File(filePath).length();
    }

    public static FileInputStream getFileInputStream(String filePath) throws Exception {
        return new FileInputStream(filePath);
    }

    public static void closeFileStream(InputStream inputStream, String filePath) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Object e) {
                LOG.error("close file {} occur an IOExcpetion {}", (Object) filePath, e);
            }
        }
    }

    public static String getFileContent(String filePath) throws Exception {
        return getFileContent(filePath, 0, Long.valueOf(getFileLength(filePath)).intValue());
    }

    public static String getFileContent(String filePath, long offset, int length) throws Exception {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = getFileInputStream(filePath);
            String fileContent = getFileContent((InputStream) fileInputStream, offset, length);
            return fileContent;
        } finally {
            closeFileStream(fileInputStream, filePath);
        }
    }

    public static String getFileContent(InputStream inputStream, long offset, int length) throws Exception {
        return new String(getFileContentByte(inputStream, offset, length), Charset.forName("ISO-8859-1"));
    }

    public static byte[] getFileContentByte(InputStream inputStream, long offset, int length) throws Exception {
        if (offset < 0 || length < 0) {
            throw new Exception("getFileContent param error");
        }
        byte[] tempBuf = new byte[length];
        inputStream.skip(offset);
        int readLen = inputStream.read(tempBuf);
        if (readLen < 0) {
            return new byte[0];
        }
        byte[] fileContent;
        if (readLen < length) {
            fileContent = new byte[readLen];
            System.arraycopy(tempBuf, 0, fileContent, 0, readLen);
        } else {
            fileContent = tempBuf;
        }
        return fileContent;
    }

    public static void remove(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public static long getFileLastModified(String filePath) throws Exception {
        if (isLegalFile(filePath)) {
            return new File(filePath).lastModified();
        }
        String errorMsg = filePath + " is not file or not exist or can't be read!";
        LOG.error(errorMsg);
        throw new Exception(errorMsg);
    }
}
