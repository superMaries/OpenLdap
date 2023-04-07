package cn.ldap.ldap.common.enums;
/**
 *  用户角色枚举
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public enum UserRoleEnum {
    //Admin
    ACCOUNT_ADMIN(0, "Admin"),
    //管理员
    USER_ADMIN(1, "管理员"),
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
     *
     * @param code
     * @param msg
     */
    UserRoleEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取对应的code
     * @return code
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
