package cn.ldap.ldap.common.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * @title: Sm2Util
 * @Author Wy
 * @Date: 2023/4/13 13:54
 * @Version 1.0
 */
@Slf4j
public class Sm2Util {

    /**
     * 验签
     *
     * @param signCert 签名证书
     * @param src      原数据
     * @param signData 签名值
     * @return true 验签成功  false 验签失败
     */
    public static boolean verify(String signCert, String src, String signData) {
        //解析证书
        X509Certificate x509Cert = null;
        try {
            byte[] cert = Base64.getDecoder().decode(signCert);
            x509Cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(cert));
        } catch (CertificateException e) {
            log.error("解析证书失败:{}", e.getMessage());
            return StaticValue.FALSE;
        }
        //获取公钥
        PublicKey publicKey = x509Cert.getPublicKey();
        // 解密签名值
        return getCipher(publicKey, signData, src);
    }

    /**
     * 解密签名值
     *
     * @param publicKey 公钥
     * @param signature 签名值
     * @param src       原数据
     * @return true 验签成功  false 验签失败
     */
    private static boolean getCipher(PublicKey publicKey, String signature, String src) {
        // 解密签名值
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(StaticValue.SM2);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedSignature = cipher.doFinal(signature.getBytes());
            // 计算哈希值
            MessageDigest digest = MessageDigest.getInstance(StaticValue.SM3);
            byte[] hash = digest.digest(src.getBytes());
            return sm2Verify(hash, decryptedSignature, publicKey);
        } catch (NoSuchAlgorithmException e) {
            log.error("解密签名值失败:{}", e.getMessage());
            return StaticValue.FALSE;
        } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("解密签名值失败:{}", e.getMessage());
            return StaticValue.FALSE;
        }
    }

    /**
     * 验签
     *
     * @param src       原数据
     * @param signature 签名值
     * @param publicKey 公钥
     * @return
     */
    private static boolean sm2Verify(byte[] src, byte[] signature, PublicKey publicKey) {
        // 验签
        Signature sign = null;
        try {
            sign = Signature.getInstance(StaticValue.SM2);
            sign.initVerify(publicKey);
            sign.update(src);
            return sign.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error("验签失败:{}", e.getMessage());
            return StaticValue.FALSE;
        }
    }
}
