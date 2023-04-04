package cn.ldap.ldap.common.enums;

public enum UserRoleEnum {
    ACCOUNT_ADMIN(0, "Admin"),
    USER_ADMIN(1, "管理员"),
    ;
    private Integer code;
    private String msg;

    UserRoleEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
