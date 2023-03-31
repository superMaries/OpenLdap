package cn.ldap.ldap.common.enums;

public enum ConfigEnum {
    MAIN_SERVICE(0, "主服务器"),
    FORM_SERVICE(1, "从服务器"),
    ;
    private Integer code;
    private String msg;

    ConfigEnum(int code, String msg) {
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
