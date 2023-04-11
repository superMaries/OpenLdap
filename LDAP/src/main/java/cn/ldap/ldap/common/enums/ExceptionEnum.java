package cn.ldap.ldap.common.enums;

/**
 * 异常枚举
 *
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public enum ExceptionEnum {
    //操作成功
    SUCCESS(200, "操作成功"),
    //用户未登录
    USER_NOT_LOGIN(800, "用户未登录"),
    //参数异常
    PARAM_ERROR(201, "参数异常"),
    PASSWD(204, "密码不可为null"),
    ACCOUNT(205, "账号不可为null"),
    //数据存储错误
    SQL_SAVA_ERROR(202, "数据存储错误"),

    //已被初始化
    USER_INIT(203, "已被初始化"),


    //系统异常
    SYSTEM_ERROR(500, "系统异常"),

    //登录失败
    USER_LOGIN_ERROR(581, "登录失败"),
    //用户不存在或已被禁用
    USER_FAIL(583, "用户不存在或已被禁用"),
    //用户不存在
    USER_NAME_FAIL(584, "用户不存在"),
    //密码为空
    USER_PASSWORD_FAIL(585, "密码为空"),
    //密码超过长度限制
    MORE_PASSWORD_LENGTH(586, "密码超过长度限制"),
    USER_ACCOUNT_ERROR(587, "账号或密码错误"),
    //未获取到配置文件信息
    NO_CONFIG(800, "未获取到配置文件信息"),
    PARAM_EMPTY(801, "参数为空"),
    PARAM_ANOMALY(802, "参数异常"),
    SQL_ERROR(803, "SQL异常"),
    POINTT_NOT_EXIT(804, "节点不存在"),
    QUERY_POINT_ERROR(805, "查询节点失败"),
    //失败
    LINUX_ERROR(5007, "失败"),
    //上传失败
    UPLOAD_ERR(5008, "上传失败"),
    //文件不存在
    FILE_NOT_EXIST(5009, "文件不存在"),
    //文件为空
    FILE_IS_EMPTY(5010, "文件为空"),
    //数据为空
    COLLECTION_EMPTY(5011, "数据为空"),
    //验签失败
    VERIFY_FAIL(5012, "验签失败"),

    ;

    /**
     * code
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * @param code    code
     * @param message 提示信息
     */
    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取对应的code
     *
     * @return code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取对应的提示信息
     *
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }
}
