package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
    public ResultVo getVersion(){
        return ResultUtil.success(loginService.getVersion());
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

    /**
     * 查看所有菜单接口
     * @param
     * @return
     */
    @GetMapping("menus")
    public List<Permission> queryMenus(){
        return loginService.queryMenus();
    }

    /**
     * 是否初始化
     * @return
     */
    @GetMapping("whetherInit")
    public ResultVo whetherInit(){
        return loginService.whetherInit();
    }

    /**
     * 获取服务模式
     * @return
     */
    @GetMapping("getServerConfig")
    public ResultVo getServerConfig(){
        return loginService.getServerConfig();
    }

    /**
     * USBKey登录
     * @param userDto
     * @return
     */
    @PostMapping("certLogin")
    public Map<String, Object> certLogin(@RequestBody UserDto userDto) {
        return loginService.certLogin(userDto);
    }

    /**
     * 用户名密码登录
      * @param loginDto
     * @return
     */
    @PostMapping("login")
    public ResultVo login(@RequestBody LoginDto loginDto) {
        return loginService.login(loginDto);
    }

    /**
     * 退出登录
     * @param
     * @param request
     * @return
     */
    @PostMapping("logout")
    public boolean logout(HttpServletRequest request) {
        return loginService.logout(request);
    }
}
