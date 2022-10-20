package Hyperledger.rest.api.openapi;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Decode {
    public static String decrypt(String signature,String publicKey) throws Exception {
        //Base64解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(signature.getBytes("UTF-8"));
        //Base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        String outStr=new String(cipher.doFinal(inputByte));
        return outStr;

    }

    public static String encrypt(String str,String privateKeyStr) throws Exception {
        //base64编码的公钥
        byte[] buffer = Base64.decodeBase64(privateKeyStr);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKey priKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
         //RAS加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,priKey);
        String outStr=Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    public static String[] getKeyPair() throws Exception {
        String[] strArray = new String[2];
        //KeyPairGenerator类用于生成公钥和密钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        //初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024,new SecureRandom());
        //生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();//得到私钥
        PublicKey publicKey = keyPair.getPublic();//得到公钥
        //得到公钥字符串
        String publicKeyString=new String(Base64.encodeBase64(publicKey.getEncoded()));
        //得到私钥字符串
        String privateKeyString=new String(Base64.encodeBase64(privateKey.getEncoded()));
        //将公钥和私钥保存到Map
        strArray[0] = publicKeyString;
        strArray[1] = privateKeyString;
        return strArray;

    }

    public static void main( String[] args ) throws Exception {
        //生成公钥和私钥
        String[] strArray = getKeyPair();
        //加密字符串
        // String password="{\"id\":3,\"timestamp\":3,\"params\":{\"account\":\"user1\"},\"key\":\"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJPC+/t7ycBHR6Gm/rnAQbdMe4zlXebJ/wWgypQR5pFq57aHlfhaS8Yr+zmv6/imf8ApePD89FL/QngLjKfWpwECAwEAAQ==\"}";
        String id = "3";
        String timestamp = "3643278248";
        String param1 = "account";
        String val1 = "user1";

        String password = id + timestamp + param1 + val1 + strArray[0];

        System.out.println(password.getBytes("UTF-8").length);
        // int keyLength = publicKey.getModulus().bitLength() / 16;
        // String[] datas = splitString(data, keyLength - 11);
        // String mi = "";
        // for (String s : datas) {
        //     mi += bcd2Str(cipher.doFinal(s.getBytes()));
        // }
        
        System.out.println("随机生成的公钥为："+strArray[0]);
        System.out.println("随机生成的私钥为："+strArray[1]);
        String passwordEn=encrypt(password,strArray[1]);
        System.out.println(password+"\t加密后的字符串为："+passwordEn);
        String passwordDe=decrypt(passwordEn,strArray[0]);
        System.out.println("还原后的字符串为："+passwordDe);
    }
}
