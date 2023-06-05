package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class FromSyncDto {

    private Integer rid;
    /**
     * 同步时间间隔
     */
    private Integer syncTime;
    /**
     * 主服务地址
     */
    private String mainServerUrl;
    /**
     * 同步节点
     */
    private String syncPoint;

    /**
     * 主服务账号
     */
    private String mainServerAccount;
    /**
     * 主服务密码
     */
    private String mainServerPassword;

    /**
     * 是否是安全的
     */
    private Boolean ifSafe;

    private Boolean ifBothWay;

    private String caCer;

    private String serverCer;

    private String serverKey;

    /**
     * 同步策略
     */
    private String type;
}
