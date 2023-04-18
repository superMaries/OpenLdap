package cn.ldap.ldap.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ObjectDataDto {

    /**
     *objectClass名称
     */
    private String objectClassName;
    /**
     * 属性集合名称
     */
    private List<String> attributesName;
}
