package Hyperledger.rest.api.openapi;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class RSAHelper {
    public static String decrypt(String signature, String publicKey) throws Exception {
        // Base64 decode encryed string
        byte[] inputByte = Base64.decodeBase64(signature.getBytes("UTF-8"));
        // public key with Base64 encoding
        byte[] decoded = Base64.decodeBase64(publicKey);
        PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        // RSA decryption
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;

    }

    public static String encrypt(String str, String privateKeyStr) throws Exception {
        // private key with Base64 encoding
        byte[] buffer = Base64.decodeBase64(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey priKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        // RAS encryption
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, priKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    public static String[] getKeyPair(String seed) throws Exception {
        String[] strArray = new String[2];
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024, new SecureRandom(seed.getBytes()));

        KeyPair keyPair = keyPairGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.encodeBase64(privateKey.getEncoded()));

        strArray[0] = publicKeyString;
        strArray[1] = privateKeyString;

        return strArray;
    }

    public static String encryptSplit(String input, String privateKeyStr) {
        String result = "";
        try {
            byte[] buffer = Base64.decodeBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey priKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            // RAS encryption
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, priKey);

            byte[] inputArray = input.getBytes();
            int inputLength = inputArray.length;
            System.out.println("Byte length needs to encrypt: " + inputLength);

            int MAX_ENCRYPT_BLOCK = 117;
            int offSet = 0;
            byte[] resultBytes = {};
            byte[] cache = {};

            while (inputLength - offSet > 0) {
                if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                    offSet += MAX_ENCRYPT_BLOCK;
                } else {
                    cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                    offSet = inputLength;
                }
                resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
            }
            result = Base64.encodeBase64String(resultBytes);
        } catch (Exception e) {
            System.out.println("rsaEncrypt error:" + e.getMessage());
        }
        System.out.println("Encryption result: " + result);
        return result;
    }

    public static String decryptSplit(String signature, String publicKey) {
        String result = "";
        try {
            byte[] inputArray = Base64.decodeBase64(signature.getBytes("UTF-8"));
            byte[] decoded = Base64.decodeBase64(publicKey);
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            // RSA decryption
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, pubKey);

            int inputLength = inputArray.length;
            System.out.println("Byte length needs to decrypt: " + inputLength);

            int MAX_ENCRYPT_BLOCK = 128;
            int offSet = 0;
            byte[] resultBytes = {};
            byte[] cache = {};

            while (inputLength - offSet > 0) {
                if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                    offSet += MAX_ENCRYPT_BLOCK;
                } else {
                    cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                    offSet = inputLength;
                }
                resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
            }
            result = new String(resultBytes);
        } catch (Exception e) {
            System.out.println("rsaEncrypt error:" + e.getMessage());
        }
        System.out.println("Decryption result: " + result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        // String[] strArray = getKeyPair("4");
        String[] strArray = {
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCE6Hc+T4/IqAgiqaE0Tvlbbo2LP6vMVRT5lnHeZwCyDfSU1PMcFGFhcCPQ2Jsz21iKCSO4eMWX4NkQi2IxJEUt9PTue++Vin+VjmVjFmHa1fCxgC44bLEJkzwcsbPcfrbPqOvvjWCQ0eDmPXpowZsNcdk+jjM6h+bB/ogaD3FcowIDAQAB",
                "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAITodz5Pj8ioCCKpoTRO+VtujYs/q8xVFPmWcd5nALIN9JTU8xwUYWFwI9DYmzPbWIoJI7h4xZfg2RCLYjEkRS309O5775WKf5WOZWMWYdrV8LGALjhssQmTPByxs9x+ts+o6++NYJDR4OY9emjBmw1x2T6OMzqH5sH+iBoPcVyjAgMBAAECgYBt2ptdBHQK3WBEDkMxMimHOjyCITGF5bk9hu09b5OymDhVoCIFTo8i84aXA8JcvWtURLOisShZHb8snpidl6YZYM70HwX0oAEfyW2TlELM8ljdmicrnaiPohYVTf2LaurdUDxRdZCJAbwBp4/BarsSfN2gj7CbIfE03h9kXC1TiQJBAMvhNu5jNQqyhP6ZS8Sv/D+Aa2LMJziFgvZBZtaXfltRcfHWIQa6IiwQ+r+7+1w1wzeM93FzmIqTDQwJeHBy0b8CQQCm4o2jefT0p/PqROQdPVwSBLFN0NDzWRn9KJLpcMt+nDWqwI++Biwr1KOncGDcTZCM10LgWGxv7f7uMVwZguYdAkEAo8bpP5rGMy+xEmzGptvQQJrCqPzizM7Do1pqaBwOTwEgDWs74JGJfeit5XP2ud4eUfOVmreHZFo4cuDwtTQnNQJARTtvbwZFLMoQUnvJ9qdh7serlpCuXoX0ViXi7J0yjo/XY8MG3tpIsNZCHlBsnng/I26Z++Ay/CxLuh0YDC1VsQJAM3chLc6ySs7DRYoS8OWGhD7xCKy0GbETi02S39xWlfT8iVpDHYUcHBNMUHR3nccchM9oLJe5n77gscLaL5Iy8w==" };
        String id = "6";
        Long timestamp = System.currentTimeMillis();
        String paramAccount = "account";
        String valAccount = "10001";

        String param1 = "fromId";
        String val1 = "10001";
        String param2 = "toId";
        String val2 = "10002";
        String param3 = "amount";
        String val3 = "600.0";

        String queryBalance = id + timestamp + paramAccount + valAccount + strArray[0];
        String querySend = id + timestamp + param1 + val1 + param2 + val2 + param3 + val3 + strArray[0];

        System.out.println("Account: " + valAccount);
        System.out.println("====================");
        System.out.println("Public key: " + strArray[0]);
        System.out.println("====================");
        System.out.println("Private key: " + strArray[1]);
        System.out.println("====================");
        String queryBalanceEn = encryptSplit(queryBalance, strArray[1]);
        System.out.println("====================");
        String queryBalanceDe = decryptSplit(queryBalanceEn, strArray[0]);
        System.out.println("====================");
        System.out.println("Compare: " + queryBalanceDe.equals(queryBalance));
        System.out.println("====================");
        String querySendEn = encryptSplit(querySend, strArray[1]);
        System.out.println("====================");
        String querySendDe = decryptSplit(querySendEn, strArray[0]);
        System.out.println("====================");
        System.out.println("Compare: " + querySendDe.equals(querySend));
    }
}
