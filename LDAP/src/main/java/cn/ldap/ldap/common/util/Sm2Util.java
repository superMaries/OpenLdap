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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * @title: Sm2Util
 * @Author Wy
 * @Date: 2023/4/13 13:54
 * @Version 1.0
 */
@Slf4j
public class Sm2Util {

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

    /**
     * 验签
     *
     * @param signCert  签名证书
     * @param src       原数据
     * @param signature 签名值
     * @return true 验签成功  false 验签失败
     */
    public static boolean verify(String signCert, String src, String signature) throws IOException {
        //解析证书
        X509Certificate x509Cert = null;
        IscJcrypt iscJcrypt = new IscJcrypt();
        String publicKey = iscJcrypt.getCertInfo(signCert, 39);
        if (signature.length() != 128) {
            ASN1InputStream decoder = new ASN1InputStream(new ByteArrayInputStream(hexToByte(signature)));

            ASN1Primitive asn1Primitive = decoder.readObject();
            ASN1Encodable r = ((DLSequence) asn1Primitive).getObjectAt(0);
            ASN1Encodable s = ((DLSequence) asn1Primitive).getObjectAt(1);

            byte[] rBytes = ((ASN1Integer) ((ASN1Sequence) asn1Primitive).getObjectAt(0)).getValue().toByteArray();
            String r1 = new String(HexUtil.encodeHex(rBytes)).substring(new String(HexUtil.encodeHex(rBytes)).length() - 64);

            byte[] sBytes = ((ASN1Integer) ((ASN1Sequence) asn1Primitive).getObjectAt(1)).getValue().toByteArray();

            String s1 = new String(HexUtil.encodeHex(sBytes)).substring(new String(HexUtil.encodeHex(sBytes)).length() - 64);

            signature = r1 + s1;
            decoder.close();

        }
        // 解密签名值
        return verifyEx(publicKey, src, signature);
    }


    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param src        原数据
     * @return 返回签名值
     */
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
}
