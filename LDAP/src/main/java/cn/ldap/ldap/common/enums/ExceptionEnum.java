package cn.ldap.ldap.common.enums;

public enum ExceptionEnum {
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统异常"),
    ;


    private Integer code;

    private String message;

    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
