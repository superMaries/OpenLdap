package cn.ldap.ldap.util;

import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author suntao
 * @create 2023/6/19
 */
public class decUtil {

    public static String decSecret(String base64Secret){
        SymmetricCrypto sm4 = new SymmetricCrypto("SM4/ECB/PKCS5Padding");

        byte[] decode = Base64.getDecoder().decode(base64Secret);
        byte[] decrypt = sm4.decrypt(decode);
        String secret = new String(decrypt);
        return secret;
    }



    public static byte[] encrypt(String plaintext) {
        try {
            byte[] key = new byte[16]; // 16 bytes key
            byte[] iv = new byte[16]; // 16 bytes initialization vector

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new SM4Engine()));
            cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

            byte[] input = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] output = new byte[cipher.getOutputSize(input.length)];

            int bytesProcessed = cipher.processBytes(input, 0, input.length, output, 0);
            cipher.doFinal(output, bytesProcessed);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(byte[] ciphertext) {
        try {
            byte[] key = new byte[16]; // 16 bytes key
            byte[] iv = new byte[16]; // 16 bytes initialization vector

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new SM4Engine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));

            byte[] output = new byte[cipher.getOutputSize(ciphertext.length)];

            int bytesProcessed = cipher.processBytes(ciphertext, 0, ciphertext.length, output, 0);
            cipher.doFinal(output, bytesProcessed);

            return new String(output, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
