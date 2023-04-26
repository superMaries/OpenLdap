package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.CertDto;
import cn.ldap.ldap.common.vo.CertVo;
import cn.ldap.ldap.common.vo.ResultVo;

/**
 * 证书信息
 *
 * @title: AdminUService
 * @Author Wy
 * @Date: 2023/4/10 14:09
 * @Version 1.0
 */
public interface CertService {
    /**
     * 获取证书信息证书信息
     *
     * @return 证书信息
     */
    ResultVo<CertVo> queryCertInfo(CertDto certDto);
}
