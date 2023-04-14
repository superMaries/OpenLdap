package cn.ldap.ldap.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ObjectDataDto {

    private String objectClassName;

    private List<String> attributesName;
}
