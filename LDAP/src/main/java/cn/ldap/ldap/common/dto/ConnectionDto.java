package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Data
public class ConnectionDto {

    /**
     * 连接地址
     */
    private String url;
    /**
     * 同步节点
     */
    private String baseDN;
    /**
     * 账号
     */
    private String account;
    /**
     * 密码
     */
    private String password;
}
