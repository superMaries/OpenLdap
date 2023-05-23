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
    /**
     * ca证书
     */
    private String caCer;
    /**
     * 服务器证书
     */
    private String serverCer;
    /**
     * 密钥
     */
    private String key;
}
