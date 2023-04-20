package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
import cn.ldap.ldap.service.LdapConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/indexRule/")
public class IndexRuleController {

    @Resource
    private IndexRuleService indexRuleService;

    @Resource
    private LdapConfigService ldapConfigService;

    /**
     * 查询索引规则接口
     * @return
     */
    @GetMapping("queryIndexRule")
    public ResultVo<List<String>> queryIndexRule(){
        return indexRuleService.queryIndexRule();
    }

    /**
     * 上传CA证书
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadCACert")
  //  @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<T> uploadCACert(@RequestParam("multipartFile") MultipartFile multipartFile){
        return ldapConfigService.uploadCACert(multipartFile);
    }

    /**
     * 上传证书
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadCert")
   // @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<T> uploadCert(@RequestParam("multipartFile") MultipartFile multipartFile){
        return ldapConfigService.uploadCert(multipartFile);
    }

    /**
     * 上传密钥
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadKey")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<T> uploadKey(@RequestParam("multipartFile") MultipartFile multipartFile){
        return ldapConfigService.uploadKey(multipartFile);
    }




}
