package cn.ldap.ldap.common.enums;
/**
 * 配置枚举
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public enum ConfigEnum {
    //主服务器
    MAIN_SERVICE(0, "主服务器"),
    //从服务器
    FORM_SERVICE(1, "从服务器"),
    ;
    /**
     * code
     */
    private Integer code;
    /**
     * 提示信息
     */
    private String msg;

    /**
     * @param code 编码
     * @param msg 提示信息
     */
    ConfigEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取枚举的code
     * @return
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取对应的提示信息
     * @return 提示信息
     */
    public String getMsg() {
        return msg;
    }
}
