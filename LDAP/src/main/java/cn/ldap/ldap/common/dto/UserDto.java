package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: UserDto
 * @Author Wy
 * @Date: 2023/3/31 10:45
 * @Version 1.0
 */
@Data
public class UserDto extends SrcModel {
    /**
     * 签名证书
     */
    private String signCert;
    /**
     * 证书序列号
     */
    private String certSn;
    /**
     * 角色
     */
    private Integer roleId = 1;
    /**
     * 证书名称
     */
    private String certName;
    /**
     * 0, "主服务器"
     * 1, "从服务器"
     */
    private Integer serviceType;

}
