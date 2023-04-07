package cn.ldap.ldap.common.enums;

/**
 * 用户状态
 *
 * @title: UserEnableEnum
 * @Author Wy
 * @Date: 2023/3/31 10:50
 * @Version 1.0
 */
public enum UserEnableEnum {
    USER_DISABLE(0, "禁用"),
    USER_ENALE(1, "启用"),
    ;
    private Integer code;
    private String msg;

    /**
     *
     * @param code
     * @param msg
     */
    UserEnableEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    /**
     * 获取对应的提code
     * @return
     */
    public  Integer getCode(){
        return  code;
    }
    /**
     * 获取对应的提示信息
     * @return
     */
    public String getMsg(){
        return msg;
    }
}
