package cn.ldap.ldap.common.enums;

public enum UserRoleEnum {
    USER_ADMIN(0, "管理员"),
    ;
    private Integer code;
    private String msg;

    UserRoleEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public  Integer getCode(){
        return  code;
    }
    public String getMsg(){
        return msg;
    }
}
