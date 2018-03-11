package sig;

import com.tls.base64_url.base64_url;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.Arrays;
import org.json.JSONObject;

public class tls_sigature
{

   //填写私钥和apkid  具体的值在登录腾讯云https://console.cloud.tencent.com/avc/appDetail/{$your_apkid}  查看,私钥的值需下载密钥对，并复制到下面
  private static final String privStr = "-----BEGIN PRIVATE KEY-----\nMIGEA***********J2V\n-----END PRIVATE KEY-----";
  private static final String pubStr = "-----BEGIN PUBLIC KEY-----\nMFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEGdQx1mdqg/+8VFXq1qnU1hZmwx9Aqblx\nAsOxCOONxZsiAgqozNcCdQEl5CRjqTkZCcy/jCCoYWzwfF9HB0ydlQ==\n-----END PUBLIC KEY-----";
  private static final long apkid = 14000*****L;
  
  public static class GenTLSSignatureResult
  {
    public String errMessage;
    public String urlSig;
    public int expireTime;
    public int initTime;
    
    public GenTLSSignatureResult()
    {
      this.errMessage = "";
      this.urlSig = "";
    }
  }
  
  public static class CheckTLSSignatureResult
  {
    public String errMessage;
    public boolean verifyResult;
    public int expireTime;
    public int initTime;
    
    public CheckTLSSignatureResult()
    {
      this.errMessage = "";
      this.verifyResult = false;
    }
  }
  
  public static String getSig(String uid)
  {
    try
    {
      GenTLSSignatureResult result = GenTLSSignatureEx(apkid, uid,privStr );
      
      return result.urlSig;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return "";
  }
  
  @Deprecated
  public static GenTLSSignatureResult GenTLSSignature(long expire, String strAppid3rd, long skdAppid, String identifier, long accountType, String privStr)
    throws IOException
  {
    GenTLSSignatureResult result = new GenTLSSignatureResult();
    
    Security.addProvider(new BouncyCastleProvider());
    Reader reader = new CharArrayReader(privStr.toCharArray());
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
    PEMParser parser = new PEMParser(reader);
    Object obj = parser.readObject();
    parser.close();
    PrivateKey privKeyStruct = converter.getPrivateKey((PrivateKeyInfo)obj);
    
    String jsonString = "{\"TLS.account_type\":\"" + accountType + "\",\"TLS.identifier\":\"" + identifier + "\",\"TLS.appid_at_3rd\":\"" + strAppid3rd + "\",\"TLS.sdk_appid\":\"" + skdAppid + "\",\"TLS.expire_after\":\"" + expire + "\"}";
    
    String time = String.valueOf(System.currentTimeMillis() / 1000L);
    String SerialString = "TLS.appid_at_3rd:" + strAppid3rd + "\nTLS.account_type:" + accountType + "\nTLS.identifier:" + identifier + "\nTLS.sdk_appid:" + skdAppid + "\nTLS.time:" + time + "\nTLS.expire_after:" + expire + "\n";
    try
    {
      Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
      signature.initSign(privKeyStruct);
      signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
      byte[] signatureBytes = signature.sign();
      
      String sigTLS = Base64.encodeBase64String(signatureBytes);
      
      JSONObject jsonObject = new JSONObject(jsonString);
      jsonObject.put("TLS.sig", sigTLS);
      jsonObject.put("TLS.time", time);
      jsonString = jsonObject.toString();
      
      Deflater compresser = new Deflater();
      compresser.setInput(jsonString.getBytes(Charset.forName("UTF-8")));
      
      compresser.finish();
      byte[] compressBytes = new byte[512];
      int compressBytesLength = compresser.deflate(compressBytes);
      compresser.end();
      
      String userSig = new String(base64_url.base64EncodeUrl(Arrays.copyOfRange(compressBytes, 0, compressBytesLength)));
      
      result.urlSig = userSig;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result.errMessage = "generate usersig failed";
    }
    return result;
  }
  
  @Deprecated
  public static CheckTLSSignatureResult CheckTLSSignature(String urlSig, String strAppid3rd, long skdAppid, String identifier, long accountType, String publicKey)
    throws DataFormatException
  {
    CheckTLSSignatureResult result = new CheckTLSSignatureResult();
    Security.addProvider(new BouncyCastleProvider());
    
    Base64 decoder = new Base64();
    
    byte[] compressBytes = base64_url.base64DecodeUrl(urlSig.getBytes(Charset.forName("UTF-8")));
    
    Inflater decompression = new Inflater();
    decompression.setInput(compressBytes, 0, compressBytes.length);
    byte[] decompressBytes = new byte[1024];
    int decompressLength = decompression.inflate(decompressBytes);
    decompression.end();
    
    String jsonString = new String(Arrays.copyOfRange(decompressBytes, 0, decompressLength));
    
    JSONObject jsonObject = new JSONObject(jsonString);
    String sigTLS = jsonObject.getString("TLS.sig");
    
    byte[] signatureBytes = decoder.decode(sigTLS.getBytes(Charset.forName("UTF-8")));
    try
    {
      String sigTime = jsonObject.getString("TLS.time");
      String sigExpire = jsonObject.getString("TLS.expire_after");
      if (System.currentTimeMillis() / 1000L - Long.parseLong(sigTime) > Long.parseLong(sigExpire))
      {
        result.errMessage = new String("TLS sig is out of date ");
        System.out.println("Timeout");
        return result;
      }
      String SerialString = "TLS.appid_at_3rd:" + strAppid3rd + "\nTLS.account_type:" + accountType + "\nTLS.identifier:" + identifier + "\nTLS.sdk_appid:" + skdAppid + "\nTLS.time:" + sigTime + "\nTLS.expire_after:" + sigExpire + "\n";
      
      Reader reader = new CharArrayReader(publicKey.toCharArray());
      PEMParser parser = new PEMParser(reader);
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      Object obj = parser.readObject();
      parser.close();
      PublicKey pubKeyStruct = converter.getPublicKey((SubjectPublicKeyInfo)obj);
      
      Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
      signature.initVerify(pubKeyStruct);
      signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
      boolean bool = signature.verify(signatureBytes);
      
      result.verifyResult = bool;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result.errMessage = "Failed in checking sig";
    }
    return result;
  }
  
  public static GenTLSSignatureResult GenTLSSignatureEx(long skdAppid, String identifier, String privStr)
    throws IOException
  {
    return GenTLSSignatureEx(skdAppid, identifier, privStr, 15552000L);
  }
  
  public static GenTLSSignatureResult GenTLSSignatureEx(long skdAppid, String identifier, String privStr, long expire)
    throws IOException
  {
    GenTLSSignatureResult result = new GenTLSSignatureResult();
    
    Security.addProvider(new BouncyCastleProvider());
    Reader reader = new CharArrayReader(privStr.toCharArray());
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
    PEMParser parser = new PEMParser(reader);
    Object obj = parser.readObject();
    parser.close();
    PrivateKey privKeyStruct = converter.getPrivateKey((PrivateKeyInfo)obj);
    
    String jsonString = "{\"TLS.account_type\":\"0\",\"TLS.identifier\":\"" + identifier + "\",\"TLS.appid_at_3rd\":\"" + 0 + "\",\"TLS.sdk_appid\":\"" + skdAppid + "\",\"TLS.expire_after\":\"" + expire + "\",\"TLS.version\": \"201512300000\"}";
    
    String time = String.valueOf(System.currentTimeMillis() / 1000L);
    String SerialString = "TLS.appid_at_3rd:0\nTLS.account_type:0\nTLS.identifier:" + identifier + "\nTLS.sdk_appid:" + skdAppid + "\nTLS.time:" + time + "\nTLS.expire_after:" + expire + "\n";
    try
    {
      Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
      signature.initSign(privKeyStruct);
      signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
      byte[] signatureBytes = signature.sign();
      
      String sigTLS = Base64.encodeBase64String(signatureBytes);
      
      JSONObject jsonObject = new JSONObject(jsonString);
      jsonObject.put("TLS.sig", sigTLS);
      jsonObject.put("TLS.time", time);
      jsonString = jsonObject.toString();
      
      Deflater compresser = new Deflater();
      compresser.setInput(jsonString.getBytes(Charset.forName("UTF-8")));
      
      compresser.finish();
      byte[] compressBytes = new byte[512];
      int compressBytesLength = compresser.deflate(compressBytes);
      compresser.end();
      String userSig = new String(base64_url.base64EncodeUrl(Arrays.copyOfRange(compressBytes, 0, compressBytesLength)));
      
      result.urlSig = userSig;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result.errMessage = "generate usersig failed";
    }
    return result;
  }
  
  public static CheckTLSSignatureResult CheckTLSSignatureEx(String urlSig, long sdkAppid, String identifier, String publicKey)
    throws DataFormatException
  {
    CheckTLSSignatureResult result = new CheckTLSSignatureResult();
    Security.addProvider(new BouncyCastleProvider());
    
    Base64 decoder = new Base64();
    
    byte[] compressBytes = base64_url.base64DecodeUrl(urlSig.getBytes(Charset.forName("UTF-8")));
    
    Inflater decompression = new Inflater();
    decompression.setInput(compressBytes, 0, compressBytes.length);
    byte[] decompressBytes = new byte[1024];
    int decompressLength = decompression.inflate(decompressBytes);
    decompression.end();
    
    String jsonString = new String(Arrays.copyOfRange(decompressBytes, 0, decompressLength));
    
    JSONObject jsonObject = new JSONObject(jsonString);
    String sigTLS = jsonObject.getString("TLS.sig");
    
    byte[] signatureBytes = decoder.decode(sigTLS.getBytes(Charset.forName("UTF-8")));
    try
    {
      String strSdkAppid = jsonObject.getString("TLS.sdk_appid");
      String sigTime = jsonObject.getString("TLS.time");
      String sigExpire = jsonObject.getString("TLS.expire_after");
      if (Integer.parseInt(strSdkAppid) != sdkAppid)
      {
        result.errMessage = new String("sdkappid " + strSdkAppid + " in tls sig not equal sdkappid " + sdkAppid + " in request");
        
        return result;
      }
      if (System.currentTimeMillis() / 1000L - Long.parseLong(sigTime) > Long.parseLong(sigExpire))
      {
        result.errMessage = new String("TLS sig is out of date");
        return result;
      }
      String SerialString = "TLS.appid_at_3rd:0\nTLS.account_type:0\nTLS.identifier:" + identifier + "\nTLS.sdk_appid:" + sdkAppid + "\nTLS.time:" + sigTime + "\nTLS.expire_after:" + sigExpire + "\n";
      
      Reader reader = new CharArrayReader(publicKey.toCharArray());
      PEMParser parser = new PEMParser(reader);
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      Object obj = parser.readObject();
      parser.close();
      PublicKey pubKeyStruct = converter.getPublicKey((SubjectPublicKeyInfo)obj);
      
      Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
      signature.initVerify(pubKeyStruct);
      signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
      boolean bool = signature.verify(signatureBytes);
      result.expireTime = Integer.parseInt(sigExpire);
      result.initTime = Integer.parseInt(sigTime);
      result.verifyResult = bool;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result.errMessage = "Failed in checking sig";
    }
    return result;
  }
}
