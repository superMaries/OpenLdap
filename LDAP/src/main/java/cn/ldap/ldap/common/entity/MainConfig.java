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

}
