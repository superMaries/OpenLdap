package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.LogDto;
import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import cn.ldap.ldap.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
@RestController
@Slf4j
@RequestMapping("/config/")
public class ConfigController {

    @Resource
    private LdapConfigService ldapConfigService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 添加配置
     * @param mainConfig
     * @return
     */
    @PostMapping("addConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.UPDATE_PARAM)
    public ResultVo<T> addConfig(@RequestBody MainConfig mainConfig) throws IOException {
        return ldapConfigService.addConfig(mainConfig);
    }

    /**
     * 开启或关闭服务
     * @param openOrClose
     * @return
     * @throws IOException
     */
    @PostMapping("setServerStatus/{openOrClose}")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.OPEN_SERVICE)
    public ResultVo<String> setServerStatus(@PathVariable("openOrClose") Boolean openOrClose) throws IOException {
       return ldapConfigService.setServerStatus(openOrClose);
    }

    /**
     * 判断服务是否启动
     * @return
     */
    @GetMapping("getServerStatus")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.LOOK_PARAM)
    public Boolean getServerStatus(){
        return ldapConfigService.getServerStatus();
    }

    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadFile")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER,operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<T> uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile){
        return ldapConfigService.uploadFile(multipartFile);
    }


    /**
     * 日志查询
     * @param logDto
     * @return
     */
    @PostMapping("queryLog")
    @OperateAnnotation(operateModel = OperateMenuEnum.LOG_MANAGER,operateType = OperateTypeEnum.OPERATE_QUERY)
    public ResultVo<List<OperationLogModel>> queryLog(@RequestBody LogDto logDto){
        return operationLogService.queryLog(logDto);
    }



}
