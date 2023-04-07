package cn.ldap.ldap.common.enums;

/**
 * 查询数据的枚举
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public enum QueryEnum {
    //查询LDAP数量
    TOTAL("total", "查询LDAP数量"),
    //获取网络吞吐量
    CERT_TOTAL("certTotal", "获取证书信息"),
    //获取信息信息
    CRL_TOTAL("crlTotal", "获取信息信息"),
    ;
    /**
     * key
     */
    private String key;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 获取对应的key
     * @return
     */
    public String getKey() {
        return key;
    }
    /**
     * 获取对应的提示信息
     * @return
     */
    public String getMsg() {
        return msg;
    }

    /**
     *
     * @param key key
     * @param msg 提示信息
     */
    QueryEnum(String key, String msg) {
        this.key = key;
        this.msg = msg;
    }
}
