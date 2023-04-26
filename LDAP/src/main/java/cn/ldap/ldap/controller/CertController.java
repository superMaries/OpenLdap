package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.CertDto;
import cn.ldap.ldap.common.vo.CertVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.CertService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 查询证书信息
 *
 * @title: CertController
 * @Author Wy
 * @Date: 2023/4/26 17:57
 * @Version 1.0
 */
@RestController
@RequestMapping("/cert/")
public class CertController {
    @Resource
    private CertService certService;

    /**
     * 获取证书信息证书信息
     *
     * @return 证书信息
     */
    @PostMapping("queryCert")
    public ResultVo<CertVo> queryCertInfo(@RequestBody CertDto certDto) {
       return certService.queryCertInfo(certDto);
    }
}
