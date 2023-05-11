package cn.ldap.ldap.common.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @title: LdapAccuntAuthEnum
 * @Author Wy
 * @Date: 2023/5/8 9:49
 * @Version 1.0
 */
public enum LdapAccuntAuthEnum {
    /**
     * 读
     */
    READ(1, "读","read"),
    /**
     * 写
     */

    WRITE(2, "写","write"),

    /**
     * 读写
     */
//    READ_AND_WRITE(3, "读写"),
    ;

    /**
     * 编码
     * @return 编码
     */
    private Integer code;
    /**
     * 提示
     *
     * @return 提示
     */
    private String msg;
    private String title;
    /**
     * 编码
     * @return 编码
     */
    public Integer getCode() {
        return code;
    }
    public String getTitle() {
        return title;
    }
    public static String getTitleByCode(Integer code) {
        for (LdapAccuntAuthEnum auth : LdapAccuntAuthEnum.values()) {
           if (Objects.equals(auth.getCode(), code)){
                    return auth.getTitle();
           }
        }
        return null;
    }
    /**
     * 提示
     *
     * @return 提示
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 构造
     *
     * @param code
     * @param msg
     */
    LdapAccuntAuthEnum(int code, String msg,String title) {
        this.msg = msg;
        this.code = code;
        this.title=title;
    }

    /**
     * 返回所有的数据
     *
     * @return
     */
    public static Map<Integer, String> getLdapAccountData() {
        Map<Integer, String> map = new HashMap<>();
        for (LdapAccuntAuthEnum auth : LdapAccuntAuthEnum.values()) {
            map.put(auth.code, auth.msg);
        }
        return map;
    }
}
