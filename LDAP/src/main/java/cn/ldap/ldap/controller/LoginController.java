package cn.ldap.ldap.controller;

import cn.hutool.crypto.SecureUtil;
import cn.ldap.ldap.service.LoginService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.etsi.uri.x01903.v13.SignedSignaturePropertiesType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/login/")
public class LoginController {
    @Resource
    private LoginService loginService;

    /**
     * 下载客户端工具接口
     * @param httpServletResponse
     * @return
     */
    @GetMapping("downClinetTool")
    public Boolean downClientTool(HttpServletResponse httpServletResponse){
        return loginService.downClientTool(httpServletResponse);
    }

    /**
     * 获取版本号
     * * @return
     */
    @GetMapping("getVersion")
    public Map<String,String> getVersion(){
        return loginService.getVersion();
    }

    /**
     * 下载用户手册
     * @return
     * @throws IOException
     */
    @GetMapping("downloadManual")
    public byte[] downloadManual() throws IOException {
        return loginService.downloadManual();
    }
}
