package cn.ldap.ldap.common.enums;

public enum QueryEnum {
    TOTAL("total", "查询LDAP数量"),
    CERT_TOTAL("certTotal", "获取网络吞吐量"),
    CRL_TOTAL("crlTotal", "获取信息信息"),
    ;
    private String key;
    private String msg;

    public String getKey() {
        return key;
    }

    public String getMsg() {
        return msg;
    }

    QueryEnum(String key, String msg) {
        this.key = key;
        this.msg = msg;
    }
}
