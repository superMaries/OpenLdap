package cn.ldap.ldap.service.impl;

import cn.byzk.util.CertUtil;
import cn.ldap.ldap.common.dto.CertDto;
import cn.ldap.ldap.common.enums.CertificateEnum;
import cn.ldap.ldap.common.util.IscSignUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.CertVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.CertService;
import isc.authclt.IscJcrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @title: CertServiceImpl
 * @Author Wy
 * @Date: 2023/4/26 18:20
 * @Version 1.0
 */
@Service
@Slf4j
public class CertServiceImpl implements CertService {

    private final Integer IS_CERT = 0;

    /**
     * 获取证书信息证书信息
     *
     * @return 证书信息
     */
    @Override
    public ResultVo<CertVo> queryCertInfo(CertDto certDto) {
        log.info("获取证书信息证书信息入参：{}", certDto);
        CertVo certVo = new CertVo();
        if (!IS_CERT.equals((certDto.getType()))) {
            certVo.setBaseStr(certDto.getCertBase64());
        } else {
            String certBase = IscSignUtil.otherToBase64(certDto.getCertBase64());
            certVo.setBaseStr(certBase);
            IscJcrypt iscJcrypt = new IscJcrypt();
            certVo.setCertNum(iscJcrypt.getCertInfo(certBase, CertificateEnum.CERT_NUMBER.getCode()));
            certVo.setCertAlgorithm(iscJcrypt.getCertInfo(certBase, CertificateEnum.CERT_ALOGRITHM.getCode()));
            certVo.setCertIssName(iscJcrypt.getCertInfo(certBase, CertificateEnum.SGD_CERT_ISSUE.getCode()));
            certVo.setCertSubName(iscJcrypt.getCertInfo(certBase, CertificateEnum.SGD_CERT_SUBUE.getCode()));
            certVo.setStartTime(iscJcrypt.getCertInfo(certBase, CertificateEnum.SGD_CERT_START_TIME.getCode()));
            certVo.setEndTime(iscJcrypt.getCertInfo(certBase, CertificateEnum.SGD_CERT_END_TIME.getCode()));
            certVo.setDeviceNo(iscJcrypt.getCertInfo(certBase, CertificateEnum.DEVICE_NO.getCode()));
        }
        return ResultUtil.success(certVo);
    }
}
