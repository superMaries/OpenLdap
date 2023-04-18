package cn.ldap.ldap.common.enums;

/**
 * 导入类型枚举
 *
 * @title: ImportEnum
 * @Author Wy
 * @Date: 2023/4/18 9:34
 * @Version 1.0
 */
public enum ImportEnum {
    ONLY_INTER(1, "仅添加"),
    ONLY_UPDATE(2, "仅更新"),
    INTER_OR_UPDATE(2, "更新或添加"),
    ;
    private Integer code;
    private String name;

    ImportEnum(Integer code, String name) {
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
