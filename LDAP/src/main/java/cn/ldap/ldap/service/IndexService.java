package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.vo.IndexVo;
import cn.ldap.ldap.common.vo.ResultVo;
import com.google.common.collect.EvictingQueue;

/**
 * @title: UserService
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public interface IndexService {
    /**
     * 获取信息信息
     * @return 获取信息信息
     */
    ResultVo<DeviceStatusRespVo> listDeviceStatus() ;
    /**
     * 获取网络吞吐量
     * @return 获取网络吞吐量
     */
    ResultVo< EvictingQueue<NetSpeedRespVo>> getNetSpeed();

    /**
     * 查询总量接口
     * 查询证书接口
     * 查询CRL接口
     * @return 总数接口
     */
    ResultVo<IndexVo>  ldapInfo();
}
