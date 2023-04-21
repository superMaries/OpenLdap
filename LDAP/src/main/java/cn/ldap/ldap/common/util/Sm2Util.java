package cn.ldap.ldap.common.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.asymmetric.SM2;
import isc.authclt.IscJcrypt;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
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


    private static final Integer CERT_INFO_NO = 39;

    private static final Integer SIGNATURE_LENGTH = 128;

    private static final Integer OBJECT_R = 0;

    private static final Integer OBJECT_S = 1;

    /**
     * 验签
     *
     * @param signCert 签名证书
     * @param src      原数据
     * @param
     * @return true 验签成功  false 验签失败
     */
    public static boolean verify(String signCert, String src, String signature) {
        //解析证书
        X509Certificate x509Cert = null;
        IscJcrypt iscJcrypt = new IscJcrypt();
        String publicKey = iscJcrypt.getCertInfo(signCert, CERT_INFO_NO);
        if (signature.length() != SIGNATURE_LENGTH) {
            ASN1InputStream decoder = new ASN1InputStream(new ByteArrayInputStream(hexToByte(signature)));
//            DERSequence sequence = (DERSequence) decoder.readObject();

            ASN1Primitive asn1Primitive = null;
            try {
                asn1Primitive = decoder.readObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ASN1Encodable r = ((DLSequence) asn1Primitive).getObjectAt(OBJECT_R);
            ASN1Encodable s = ((DLSequence) asn1Primitive).getObjectAt(OBJECT_S);

            byte[] rBytes = ((ASN1Integer) ((ASN1Sequence) asn1Primitive).getObjectAt(OBJECT_R)).getValue().toByteArray();
            String rStr = new String(HexUtil.encodeHex(rBytes)).substring(new String(HexUtil.encodeHex(rBytes)).length() - 64);

            byte[] sBytes = ((ASN1Integer) ((ASN1Sequence) asn1Primitive).getObjectAt(OBJECT_S)).getValue().toByteArray();

            String sStr = new String(HexUtil.encodeHex(sBytes)).substring(new String(HexUtil.encodeHex(sBytes)).length() - 64);

            signature = rStr + sStr;

            try {
                decoder.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // 解密签名值
        return verifyEx(publicKey, src, signature);
    }


    public static String sign(String privateKey, String src) {
        ECPrivateKeyParameters ecPrivateKeyParameters = BCUtil.toSm2Params(privateKey);
        SM2 sm2 = new SM2(ecPrivateKeyParameters, null);
        sm2.usePlainEncoding();
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        byte[] sign = sm2.sign(src.getBytes(), null);
        String newSign = HexUtil.encodeHexStr(sign);
        return newSign;
    }

    /**
     * @param publicKey 公钥
     * @param src       原数据
     * @param signData  签名值
     * @return
     */
    public static boolean verifyEx(String publicKey, String src, String signData) {
        publicKey = publicKey.length() == 130 ? publicKey.substring(2, publicKey.length()) : publicKey;
        String xhex = publicKey.substring(0, 64);
        String yhex = publicKey.substring(64, 128);
        ECPublicKeyParameters ecPublicKeyParameters = BCUtil.toSm2Params(xhex, yhex);
        //创建sm2 对象
        SM2 sm2 = new SM2(null, ecPublicKeyParameters);
        sm2.usePlainEncoding();
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        boolean verify = sm2.verify(src.getBytes(), HexUtil.decodeHex(signData));
        log.info("数据: {}", HexUtil.encodeHexStr(src.getBytes()));
        return verify;
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

    public static byte[] hexToByte(String data) {
        if (data == null) {
            return null;
        } else {
            int len = data.length();
            if (len % 2 != 0) {
                return null;
            } else {
                String regex = "^[A-Fa-f0-9]+$";
                if (!data.matches(regex)) {
                    return null;
                } else {
                    byte[] retByte = new byte[len / 2];
                    String tmpStr = "";

                    try {
                        for (int i = 0; i < len; i += 2) {
                            tmpStr = data.substring(i, i + 2);
                            retByte[i / 2] = (byte) Integer.parseInt(tmpStr, 16);
                        }

                        return retByte;
                    } catch (NumberFormatException var7) {
                        var7.printStackTrace();
                        System.out.println("hexToByte:转换的字符串中含有非16进制的字符！");
                        log.error(var7.getStackTrace().toString());
                        return null;
                    }
                }
            }
        }
    }
}
