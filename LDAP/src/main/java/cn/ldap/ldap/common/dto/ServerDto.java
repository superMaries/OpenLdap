package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class ServerDto {

    private Boolean openOrClose;

    private String port;

    /**
     * SSL认证策略 never单向 demand 双向
     */
    private String sslAuthStrategy;
}
