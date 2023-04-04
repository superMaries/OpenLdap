package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.entity.MainConfig;
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
     * 添加配置
     * @param mainConfig
     * @return
     */
    @PostMapping("addConfig")
    public ResultVo addConfig(@RequestBody MainConfig mainConfig){
        return ldapConfigService.addConfig(mainConfig);
    }

    /**
     * 开启或关闭服务
     * @param openOrClose
     * @return
     * @throws IOException
     */
    @PostMapping("setServerStatus/{openOrClose}")
    public ResultVo setServerStatus(@PathVariable("openOrClose") Boolean openOrClose) throws IOException {
       return ldapConfigService.setServerStatus(openOrClose);
    }

    /**
     * 判断服务是否启动
     * @return
     */
    @GetMapping("getServerStatus")
    public Boolean getServerStatus(){
        return ldapConfigService.getServerStatus();
    }

    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @PostMapping("uploadFile")
    public ResultVo uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile){
        return ldapConfigService.uploadFile(multipartFile);
    }

}
