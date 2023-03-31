package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import com.google.common.collect.EvictingQueue;

/**
 * @title: UserService
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public interface IndexService {
    Object list();

    /**
     * 获取信息信息
     * @return
     */
    DeviceStatusRespVo listDeviceStatus() ;
    /**
     * 获取网络吞吐量
     * @return
     */
    EvictingQueue<NetSpeedRespVo> getNetSpeed();
}
