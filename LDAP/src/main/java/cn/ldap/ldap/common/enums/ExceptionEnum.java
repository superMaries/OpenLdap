package cn.ldap.ldap.common.enums;

public enum ExceptionEnum {
    SUCCESS(200, "操作成功"),
    USER_NOT_LOGIN(800,"用户未登录"),
    PARAM_ERROR(201,"参数异常"),
    SQL_SAVA_ERROR(202,"数据存储错误"),
    SYSTEM_ERROR(500, "系统异常"),
    USER_LOGIN_ERROR(581, "登录失败"),
    USER_FAIL(583, "用户不存在或已被禁用"),
    USER_NAME_FAIL(584, "用户不存在"),
    USER_PASSWORD_FAIL(585, "密码不正确"),
    NO_CONFIG(800,"未获取到配置文件信息"),

    MORE_PASSWORD_LENGTH(586,"密码超过长度限制"),
    LINUX_ERROR(5007,"失败"),
    UPLOAD_ERR(5008,"上传失败"),
    FILE_NOT_EXIST(5009,"文件不存在"),
    FILE_IS_EMPTY(5010,"文件为空"),
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
