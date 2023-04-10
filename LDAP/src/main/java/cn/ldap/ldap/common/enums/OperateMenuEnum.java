package cn.ldap.ldap.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: OperateMenuEnum
 * @Author Wy
 * @Date: 2023/3/31 15:45
 * @Version 1.0
 */
public enum OperateMenuEnum {

    //用户管理
    USER_MANAGER(1,"用户管理"),
    //首页
    INDEX_MANAGER(2,"首页"),
    //目录树
    CATALOGUE_MANAGER(3,"目录树"),
    //参数配置
    PARAM_MANAGER(4,"参数配置"),
    //日志管理
    LOG_MANAGER(5,"日志管理"),
    ADMIN_MANAGER(6,"管理员管理"),

    ;

    /**
     *
     * @return 返回所有的数据
     */
    public static Map<String,String> getMap(){
        Map<String,String> maps=new HashMap<>();
        for (OperateMenuEnum operateMenuEnum : values()) {
            maps.put(operateMenuEnum.name, operateMenuEnum.name);
        }
        return  maps;
    }

    /**
     * code
     */
    private Integer code;
    /**
     * 提示信息
     */
    private String name;

    /**
     *
     * @param code code
     * @param name 提示信息
     */
    OperateMenuEnum(Integer code,String name) {
        this.code = code;
        this.name = name;
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
     * @return  提示信息
     */
    public String getName() {
        return name;
    }
}
