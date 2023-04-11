package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class SyncDto {
    /**
     * 触发同步最大数量
     */
    private Integer triggerSyncMaxNum;

    /**
     * 触发时间间隔
     */
    private Integer syncTimeInterval;

}
