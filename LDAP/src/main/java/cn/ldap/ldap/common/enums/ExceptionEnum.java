package cn.ldap.ldap.common.enums;

public enum ExceptionEnum {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(201,"参数异常"),
    SQL_SAVA_ERROR(202,"数据存储错误"),
    SYSTEM_ERROR(500, "系统异常"),
    USER_LOGIN_ERROR(581, "登录失败"),
    USER_FAIL(583, "用户不存在或已被禁用"),
    USER_NAME_FAIL(584, "用户不存在"),
    USER_PASSWORD_FAIL(585, "密码不正确"),
    NO_CONFIG(800,"未获取到配置文件信息"),
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
