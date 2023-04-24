package cn.ldap.ldap.common.util;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.ldap.ldap.common.enums.CertificateEnum;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;

import isc.authclt.IscJcrypt;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 获取证书信息
 *
 * @title: IscSignUtil
 * @Author Wy
 * @Date: 2023/4/19 9:51
 * @Version 1.0
 */
@Slf4j
public class IscSignUtil {
    /**
     * 判断证书格式 并将其他证书格式转成base64格式证书
     * 1、判断是否是base64
     * 2、如果不是base64 则判断是否有ben----开头 end--- 结尾
     * 3、如果没有 执行1 、2
     *
     * @param cert
     * @return
     */
    public static String otherToBase64(String cert) {
        //判断文件是否是64
        boolean isBase64 = Pattern.matches(StaticValue.BASE64_PATTERN, cert);
        //解码
        byte[] decode = Base64Decoder.decode(cert.getBytes());

        //是base 64 字符串
        if (isBase64) {
            //字节转String
            cert = new String(decode);

            //移处start 和 end 结束标记
            cert = replaceStartAndEnd(cert);

            //是base 64 字符串
            if (Pattern.matches(StaticValue.BASE64_PATTERN, cert)) {

                //解码
                decode = Base64Decoder.decode(cert.getBytes());

                //字节转String
                cert = new String(decode);

                //移处start 和 end 结束标记
                cert = replaceStartAndEnd(cert);
                //是base 64 字符串 抛出异常
                if (Pattern.matches(StaticValue.BASE64_PATTERN, cert)) {
                    log.error("字符格式错误");
                    throw new SysException(ExceptionEnum.STR_ERROR);
                }
            }
        }

        cert = Base64Encoder.encode(decode);
        return cert;
    }

    /**
     * per 证书格式转换base64
     *
     * @param cert
     * @return
     */
    public static String replaceStartAndEnd(String cert) {
        cert = cert.replaceAll(StaticValue.RN, StaticValue.REPLACE);
        if (cert.startsWith(StaticValue.BEGIN_CERTIFICATE)
                && cert.contains(StaticValue.END_CERTIFICATE)) {
            /**
             * 将证书中的-----BEGIN CERTIFICATE-----
             * 和 -----END CERTIFICATE----- 移除
             */
            cert = cert.replaceAll(StaticValue.BEGIN_CERTIFICATE, StaticValue.REPLACE)
                    .replaceAll(StaticValue.RN, StaticValue.REPLACE)
                    .replaceAll(StaticValue.N, StaticValue.REPLACE)
                    .replaceAll(StaticValue.END_CERTIFICATE, StaticValue.REPLACE);
        }
        return cert;
    }

    /**
     * @param cert       证书Base64
     * @param certInfoNo 编码
     * @return
     */
    public String getCertInfo(String cert, Integer certInfoNo) {
        IscJcrypt iscJcrypt = new IscJcrypt();
        return iscJcrypt.getCertInfo(cert, certInfoNo);
    }
}
