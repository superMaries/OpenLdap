package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: DeviceStatusRespVo
 * @Author Wy
 * @Date: 2023/3/31 11:20
 * @Version 1.0
 */
@Data
public class DeviceStatusRespVo {
    /**
     * CPU使用率
     */
    private float cpuRate;
    /**
     * 内存使用率
     */
    private int memoryRate;

    /**
     * 磁盘使用率
     */
    private float disRate;

    /**
     * LDAP 服务状态
     */
    private Boolean serverStatus;
}
