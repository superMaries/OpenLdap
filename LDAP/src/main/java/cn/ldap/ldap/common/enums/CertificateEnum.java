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
    SGD_CERT_ISSUE(9, "颁发者名称"),
    SGD_CERT_SUBUE(27, "使用者名称"),
    SGD_CERT_START_TIME(21,"证书开始时间"),
    SGD_CERT_END_TIME(22,"证书结束时间"),
    DEVICE_NO(70,"设备号"),

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
