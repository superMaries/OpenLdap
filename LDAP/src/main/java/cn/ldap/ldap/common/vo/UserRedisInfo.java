package cn.ldap.ldap.common.vo;

import lombok.Data;

@Data
public class UserRedisInfo {

    private Integer id;
    private Integer roleId;

    private String  certData;

    private  String  certName;

    private  String certNum;

    private String roleName;

}
