package com.qcloud.cos.common_utils;

import com.qcloud.cos.common_utils.RequestBodyKey.UploadParts;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonCodecUtils {
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final Logger LOG = LoggerFactory.getLogger(CommonCodecUtils.class);

    public static String Base64Encode(byte[] binaryData) {
        return new String(Base64.encodeBase64(binaryData, false), Charsets.UTF_8);
    }

    public static String getBufferSha1(byte[] contentBuffer) throws Exception {
        return DigestUtils.sha1Hex(contentBuffer);
    }

    public static String getEntireFileSha1(String filePath) throws Exception {
        InputStream fileInputStream = null;
        try {
            fileInputStream = CommonFileUtils.getFileInputStream(filePath);
            String sha1Digest = DigestUtils.sha1Hex(fileInputStream);
            try {
                CommonFileUtils.closeFileStream(fileInputStream, filePath);
                return sha1Digest;
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e2) {
            String errMsg = "getFileSha1 occur a exception, file:" + filePath + ", exception:" + e2.toString();
            LOG.error(errMsg);
            throw new Exception(errMsg);
        } catch (Throwable th) {
            try {
                CommonFileUtils.closeFileStream(fileInputStream, filePath);
            } catch (Exception e22) {
                throw e22;
            }
        }
    }

    public static String getSlicePartSha1(String localPath, int sliceSize, StringBuilder entireSha1Builder) throws Exception {
        if (sliceSize > 1048576) {
            sliceSize = 1048576;
        }
        JSONArray jsonArray = new JSONArray();
        try {
            int totalCount;
            long sliceOffset;
            int maxRead;
            byte[] contentBuf;
            JSONObject sliceJson;
            CommonSha1Utils sha1Utils = new CommonSha1Utils();
            sha1Utils.init();
            InputStream fileInput = CommonFileUtils.getFileInputStream(localPath);
            long fileLength = CommonFileUtils.getFileLength(localPath);
            int sliceCount = new Long((((long) (sliceSize - 1)) + fileLength) / ((long) sliceSize)).intValue();
            for (int sliceIndex = 0; sliceIndex < sliceCount - 1; sliceIndex++) {
                totalCount = 0;
                sliceOffset = ((long) sliceIndex) * ((long) sliceSize);
                while (totalCount < sliceSize) {
                    maxRead = sliceSize - totalCount;
                    if (maxRead > 1024) {
                        maxRead = 1024;
                    }
                    contentBuf = new byte[maxRead];
                    fileInput.read(contentBuf, 0, maxRead);
                    sha1Utils.update(contentBuf);
                    totalCount += maxRead;
                }
                sliceJson = new JSONObject();
                sliceJson.put("offset", sliceOffset);
                sliceJson.put(UploadParts.DATA_LEN, totalCount);
                sliceJson.put(UploadParts.DATA_SHA, (Object) sha1Utils.dumpTempState());
                jsonArray.put(sliceIndex, (Object) sliceJson);
            }
            sliceOffset = ((long) (sliceCount - 1)) * ((long) sliceSize);
            int leftSlice = new Long(fileLength - sliceOffset).intValue();
            totalCount = 0;
            while (totalCount < leftSlice) {
                maxRead = leftSlice - totalCount;
                if (maxRead > 1024) {
                    maxRead = 1024;
                }
                contentBuf = new byte[maxRead];
                fileInput.read(contentBuf, 0, maxRead);
                sha1Utils.update(contentBuf);
                totalCount += maxRead;
            }
            sha1Utils.finish();
            entireSha1Builder.append(sha1Utils.digout());
            sliceJson = new JSONObject();
            sliceJson.put("offset", sliceOffset);
            sliceJson.put(UploadParts.DATA_LEN, totalCount);
            sliceJson.put(UploadParts.DATA_SHA, (Object) sha1Utils.digout());
            jsonArray.put(sliceCount - 1, (Object) sliceJson);
            CommonFileUtils.closeFileStream(fileInput, localPath);
            return jsonArray.toString();
        } catch (Exception e) {
            LOG.error("getSlicePartSha1 occur a error, filePath:{}, sliceSize:{}, exception:{}", localPath, Integer.valueOf(sliceSize), e.toString());
            throw e;
        } catch (Throwable th) {
            CommonFileUtils.closeFileStream(null, localPath);
        }
    }

    public static String getSlicePartSha1(byte[] contentBuffer, int sliceSize, StringBuilder entireSha1Builder) throws Exception {
        Exception e;
        Throwable th;
        if (sliceSize > 1048576) {
            sliceSize = 1048576;
        }
        JSONArray jsonArray = new JSONArray();
        InputStream fileInput = null;
        try {
            CommonSha1Utils sha1Utils = new CommonSha1Utils();
            sha1Utils.init();
            InputStream fileInput2 = new ByteArrayInputStream(contentBuffer);
            try {
                int totalCount;
                long sliceOffset;
                int maxRead;
                byte[] contentBuf;
                JSONObject sliceJson;
                long fileLength = (long) contentBuffer.length;
                int sliceCount = new Long((((long) (sliceSize - 1)) + fileLength) / ((long) sliceSize)).intValue();
                for (int sliceIndex = 0; sliceIndex < sliceCount - 1; sliceIndex++) {
                    totalCount = 0;
                    sliceOffset = ((long) sliceIndex) * ((long) sliceSize);
                    while (totalCount < sliceSize) {
                        maxRead = sliceSize - totalCount;
                        if (maxRead > 1024) {
                            maxRead = 1024;
                        }
                        contentBuf = new byte[maxRead];
                        fileInput2.read(contentBuf, 0, maxRead);
                        sha1Utils.update(contentBuf);
                        totalCount += maxRead;
                    }
                    sliceJson = new JSONObject();
                    sliceJson.put("offset", sliceOffset);
                    sliceJson.put(UploadParts.DATA_LEN, totalCount);
                    sliceJson.put(UploadParts.DATA_SHA, (Object) sha1Utils.dumpTempState());
                    jsonArray.put(sliceIndex, (Object) sliceJson);
                }
                sliceOffset = ((long) (sliceCount - 1)) * ((long) sliceSize);
                int leftSlice = new Long(fileLength - sliceOffset).intValue();
                totalCount = 0;
                while (totalCount < leftSlice) {
                    maxRead = leftSlice - totalCount;
                    if (maxRead > 1024) {
                        maxRead = 1024;
                    }
                    contentBuf = new byte[maxRead];
                    fileInput2.read(contentBuf, 0, maxRead);
                    sha1Utils.update(contentBuf);
                    totalCount += maxRead;
                }
                sha1Utils.finish();
                entireSha1Builder.append(sha1Utils.digout());
                sliceJson = new JSONObject();
                sliceJson.put("offset", sliceOffset);
                sliceJson.put(UploadParts.DATA_LEN, totalCount);
                sliceJson.put(UploadParts.DATA_SHA, (Object) sha1Utils.digout());
                jsonArray.put(sliceCount - 1, (Object) sliceJson);
                if (fileInput2 != null) {
                    try {
                        fileInput2.close();
                    } catch (IOException e2) {
                    }
                }
                return jsonArray.toString();
            } catch (Exception e3) {
                e = e3;
                fileInput = fileInput2;
                try {
                    LOG.error("getSlicePartSha1 from buffer occur a error, sliceSize:{}, exception:{}", Integer.valueOf(sliceSize), e.toString());
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInput != null) {
                        try {
                            fileInput.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInput = fileInput2;
                if (fileInput != null) {
                    fileInput.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            LOG.error("getSlicePartSha1 from buffer occur a error, sliceSize:{}, exception:{}", Integer.valueOf(sliceSize), e.toString());
            throw e;
        }
    }

    public static byte[] HmacSha1(byte[] binaryData, String key) throws Exception {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(key.getBytes(), HMAC_SHA1));
            return mac.doFinal(binaryData);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("mac not find algorithm {}", HMAC_SHA1);
            throw e;
        } catch (InvalidKeyException e2) {
            LOG.error("mac init key {} occur a error {}", (Object) key, e2.toString());
            throw e2;
        } catch (IllegalStateException e3) {
            LOG.error("mac.doFinal occur a error {}", e3.toString());
            throw e3;
        }
    }

    public static byte[] HmacSha1(String plainText, String key) throws Exception {
        return HmacSha1(plainText.getBytes(), key);
    }
}
