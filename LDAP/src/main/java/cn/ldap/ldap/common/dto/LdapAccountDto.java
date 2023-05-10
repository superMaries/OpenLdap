package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: LdapAccountDto
 * @Author Wy
 * @Date: 2023/5/8 9:42
 * @Version 1.0
 */
@Data
public class LdapAccountDto {
    private String account;
    private String pwd;
    /**
     * 1 读
     * 2 写
     * 3 读写
     */
    private String auth;

}
