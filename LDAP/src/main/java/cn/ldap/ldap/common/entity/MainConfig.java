package cn.ldap.ldap.common.entity;

import lombok.Data;

@Data
public class MainConfig {
    /**
     * 日志文件目录
     */
    private String logLevelDirectory;

    /**
     * 日志输出等级
     */
    private Integer logLevel;

    /**
     * 触发同步最大数量
     */
    private Integer triggerSyncMaxNum;

    /**
     * 触发时间间隔
     */
    private Integer syncTimeInterval;

    /**
     * 是否开启匿名访问
     */
    private Boolean anonymousAccess;

    /**
     * 会话日志最大数量
     */
    private Integer talkMaxNumber;
    /**
     * 是否开启ssl认证
     */
    private Boolean SSL;

    /**
     * SSL认证策略 never单向 demand 双向
     */
    private String sslAuthStrategy;

    /**
     * 开启端口号
     */
    private String serverPort;
}
