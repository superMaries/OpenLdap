package cn.ldap.ldap.common.enums;

public enum AdminVerifyEnum {
    SIGN_SUCCESS(0, "验签成功"),
    SIGN_ERROR(1, "验签失败"),
    NOT_SIGN(999, "--"),
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
    AdminVerifyEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
