package cn.ldap.ldap.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作类型
 */
public enum OperateTypeEnum {
    //用户登录、注册、登出
    USER_LOGIN(401,"登录"),
    USER_REGIS(402,"注册"),
    USER_LOGOUT(403,"登出"),
    USER_INIT(404,"用户初始化"),
    DOWN_CLIENT(405,"下载客户端"),
    LOOK_MANUAl(406,"查看用户手册"),

    //操作日志
    OPERATE_QUERY(501,"查询日志"),


    //    目录树
    LOOK_DATA(601,"查询数据"),

    //参数配置
    LOOK_PARAM(701,"查询参数配置"),
    UPDATE_PARAM(702,"更新参数配置"),
    ;
    public static Map<String,String> getMap(){
        java.util.Map<java.lang.String, java.lang.String> maps=new HashMap<>();
        for (OperateTypeEnum operateMenuEnum : values()) {
            maps.put(operateMenuEnum.name, operateMenuEnum.name);
        }
        return  maps;
    }

    private Integer code;
    private String name;


    OperateTypeEnum(Integer code,String name) {
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
