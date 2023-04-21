package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Data
@TableName("ssl_config")
public class SSLConfig {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 标准协议操作
     */
    @TableField("operation")
    private Boolean operation;
    /**
     * 安全协议操作
     */
    @TableField("safe_operation")
    private Boolean safeOperation;

    @TableField("port")
    private String port;

    @TableField("safe_port")
    private String safePort;

    /**
     * SSL认证策略 never单向 demand 双向
     */
    @TableField("ssl_auth")
    private String sslAuthStrategy;

    @TableField("ca_cert_name")
    private String caName;
    @TableField("server_cert_name")
    private String serverName;

    @TableField("key_name")
    private String keyName;
}
