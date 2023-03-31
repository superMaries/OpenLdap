package cn.ldap.ldap.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum LogStateTypeEnum {
    /**
     * 失败.
     */
    FAIL(0,"失败"),

    /**
     * 成功.
     */
    SUCCEED(1,"成功")
    ;

    /**
     * 名称.
     */
    private  Integer code;

    @JsonValue
    private  String name;

    public static Map<String,String> getMap(){
        Map<String,String> maps=new HashMap<>();
        for (LogStateTypeEnum logStateTypeEnum : values()) {
            maps.put(logStateTypeEnum.name, logStateTypeEnum.name);
        }
        return  maps;
    }

    LogStateTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public Integer getCode() {
        return code;
    }
}
