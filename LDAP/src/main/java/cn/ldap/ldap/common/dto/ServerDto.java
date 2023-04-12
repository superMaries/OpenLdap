package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class ServerDto {

    /**
     * 标准协议操作
     */
    private Boolean operation;
    /**
     * 安全协议操作
     */
    private Boolean safeOperation;

    private String port;

    private String safePort;

    /**
     * SSL认证策略 never单向 demand 双向
     */
    private String sslAuthStrategy;
}
