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

    USER_MANAGER(1,"用户管理"),
    INDEX_MANAGER(2,"首页"),
    CATALOGUE_MANAGER(3,"目录树"),
    PARAM_MANAGER(4,"参数配置"),
    LOG_MANAGER(5,"日志管理"),

    ;

    public static Map<String,String> getMap(){
        Map<String,String> maps=new HashMap<>();
        for (OperateMenuEnum operateMenuEnum : values()) {
            maps.put(operateMenuEnum.name, operateMenuEnum.name);
        }
        return  maps;
    }

    private Integer code;
    private String name;


    OperateMenuEnum(Integer code,String name) {
        this.code = code;
        this.name = name;
    }


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
