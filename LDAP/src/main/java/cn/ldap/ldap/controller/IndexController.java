package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.vo.IndexVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexService;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @title: IndexController
 * @Author Wy
 * @Date: 2023/3/31 11:18
 * @Version 1.0
 */
@Data
@RestController
@RequestMapping("/index/")
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 获取信息信息
     *
     * @return
     */
    @PostMapping("device/status/list")
    public ResultVo<DeviceStatusRespVo> listDeviceStatus() {
        return indexService.listDeviceStatus();
    }

    /**
     * 获取网络吞吐量
     *
     * @return
     */
    @PostMapping("net/speed/list")
    public ResultVo<EvictingQueue<NetSpeedRespVo>> getNetSpeed() {
        return indexService.getNetSpeed();
    }

    /**
     * 查询总量接口
     * 查询证书接口
     * 查询CRL接口
     */
    @PostMapping("ldap/info")
    public ResultVo<IndexVo> ldapInfo() {
        return indexService.ldapInfo();
    }

    /**
     * 返回ldap 总数接口
     *
     * @return 返回ldap 总数接口
     */
    @PostMapping("ldap/num")
    public ResultVo<Long> ldapTotal() {
        return indexService.ldapTotal();
    }

    /**
     * @return 返回Crl数量
     */
    @PostMapping("ldap/crlNum")
    public ResultVo<Long> ldapCrlNum() {
        return indexService.ldapCrlNum();
    }

    /**
     * 查询证书接口
     *
     * @return 查询证书接口
     */
    @PostMapping("ldap/certNum")
    public ResultVo<Long> ldapCertNum() {
        return indexService.ldapCertNum();
    }

}



