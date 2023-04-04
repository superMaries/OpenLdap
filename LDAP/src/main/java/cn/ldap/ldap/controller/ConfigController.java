package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/config/")
public class ConfigController {

    @Resource
    private LdapConfigService ldapConfigService;

    /**
     * 更新参数配置
     *
     * @param mainConfig
     * @return
     */
    @PostMapping("addConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPDATE_PARAM)
    public ResultVo addConfig(@RequestBody MainConfig mainConfig) {
        return ldapConfigService.addConfig(mainConfig);
    }

    /**
     * 开启或关闭服务
     *
     * @param openOrClose
     * @return
     * @throws IOException
     */
    @PostMapping("setServerStatus/{openOrClose}")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.OPEN_SERVICE)
    public ResultVo setServerStatus(@PathVariable("openOrClose") Boolean openOrClose) throws IOException {
        return ldapConfigService.setServerStatus(openOrClose);
    }

    /**
     * 判断服务是否启动
     *
     * @return
     */
    @GetMapping("getServerStatus")
    public Boolean getServerStatus() {
        return ldapConfigService.getServerStatus();
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadFile")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile) {
        return ldapConfigService.uploadFile(multipartFile);
    }

}
