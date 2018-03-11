package com.qcloud.cos.sign;

import com.qcloud.cos.common_utils.CommonCodecUtils;
import com.qcloud.cos.common_utils.CommonPathUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;
import java.util.Random;

public class Sign {
   

      //需要修改这里 三个参数分别为SecretId  SecretId   SecretKey 具体值登录腾讯云https://console.cloud.tencent.com/cam/capi查看
    public static final Credentials cred = new Credentials(125*******, "AKIDIPHvLWd7Y*********", "kFWYKr9D1atx*******");

    public static Credentials getCred() {
        return cred;
    }

    private static String appSignatureBase(Credentials cred, String bucketName, String cosPath, long expired, boolean uploadFlag) throws AbstractCosException {
        String fileId;
        long appId = cred.getAppId();
        String secretId = cred.getSecretId();
        String secretKey = cred.getSecretKey();
        long now = System.currentTimeMillis() / 1000;
        int rdm = Math.abs(new Random().nextInt());
        if (uploadFlag) {
            fileId = String.format("/%d/%s%s", new Object[]{Long.valueOf(appId), bucketName, cosPath});
        } else {
            fileId = cosPath;
        }
        fileId = CommonPathUtils.encodeRemotePath(fileId);
        String plainText = String.format("a=%s&k=%s&e=%d&t=%d&r=%d&f=%s&b=%s", new Object[]{Long.valueOf(appId), secretId, Long.valueOf(expired), Long.valueOf(now), Integer.valueOf(rdm), fileId, bucketName});
        try {
            byte[] hmacDigest = CommonCodecUtils.HmacSha1(plainText, secretKey);
            byte[] signContent = new byte[(hmacDigest.length + plainText.getBytes().length)];
            System.arraycopy(hmacDigest, 0, signContent, 0, hmacDigest.length);
            System.arraycopy(plainText.getBytes(), 0, signContent, hmacDigest.length, plainText.getBytes().length);
            return CommonCodecUtils.Base64Encode(signContent);
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public static String getPeriodEffectiveSign(String bucketName, String cosPath, Credentials cred, long expired) throws AbstractCosException {
        return appSignatureBase(cred, bucketName, cosPath, expired, true);
    }

    public static String getSign() {
        try {
             
             //生成签名  需要修改的参数分别为bucket名称（tino）    储存路径（/） 生效时间（最后一个参数） 加500表示签名当前时间到之后的500秒内有效    
            return getPeriodEffectiveSign("tino", "/", getCred(), (System.currentTimeMillis() / 1000) + 500);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
