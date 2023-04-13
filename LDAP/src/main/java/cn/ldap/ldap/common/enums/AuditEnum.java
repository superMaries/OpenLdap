package cn.ldap.ldap.common.enums;

/**
 * 审计枚举
 *
 * @title: StaticValue
 * @Author Wy
 * @Date: 2023/4/7 11:48
 * @Version 1.0
 */
public enum AuditEnum {
    /**
     * 未审计
     */
    NOT_AUDIT(0, "未审计"),
    /**
     * 审计
     */
    AUDIT(1, "审计"),
    /**
     * 未知
     */
    UNKNOWN(999, "未知"),

    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 提示
     */
    private String msg;

    /**
     * @return 返回编码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @return 返回提示
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 构造
     *
     * @param code 编码
     * @param msg  提示
     */
    AuditEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
