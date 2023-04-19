package cn.ldap.ldap.common.enums;

/**
 * 证书枚举
 *
 * @title: StaticValue
 * @Author Wy
 * @Date: 2023/4/7 11:48
 * @Version 1.0
 */
public enum CertificateEnum {
    CERT_NUMBER(1, "证书序列号"),
    CERT_ALOGRITHM(3, "算法"),
    CERT_NAME(27, "证书名称"),
    SGD_EXT_AUTHORITYKEYIDENTIFIER_INFO(75, "颁发者密钥标识符"),
    SGD_EXT_SUBJECTKEYIDENTIFIER_INFO(76, "证书持有者密钥标识符"),
    ;

    private Integer code;
    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    CertificateEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
