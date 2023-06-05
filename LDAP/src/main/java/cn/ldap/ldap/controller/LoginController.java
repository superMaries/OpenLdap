package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/login/")
public class LoginController {
    @Resource
    private LoginService loginService;

    /**
     * 下载客户端工具接口
     *
     * @param httpServletResponse
     * @return
     */
    @GetMapping("downClinetTool")
    public Boolean downClientTool(HttpServletResponse httpServletResponse) {
        return loginService.downClientTool(httpServletResponse);
    }

    /**
     * 获取版本号
     * * @return
     */
    @GetMapping("getVersion")
    public ResultVo<Map<String, String>> getVersion() {
        return loginService.getVersion();
    }

    /**
     * 下载用户手册
     *
     * @return
     * @throws IOException
     */
    @GetMapping("downloadManual")
//    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.LOOK_MANUAl)
    public Boolean downloadManual(HttpServletResponse httpServletResponse) throws IOException {
        return loginService.downloadManual(httpServletResponse);
    }

    /**
     * 查看所有菜单接口
     *
     * @param
     * @return
     */
    @PostMapping("menus")
    public  ResultVo<List<Permission>> queryMenus(HttpServletRequest request) {
            return loginService.queryMenus(request);
    }

//    /**
//     * 是否初始化
//     *
//     * @return
//     */
//    @GetMapping("whetherInit")
//    public ResultVo<String> whetherInit() {
//        return loginService.whetherInit();
//    }

    /**
     * 获取服务模式
     *
     * @return
     */
    @GetMapping("getServerConfig")
    public ResultVo<String> getServerConfig() {
        return loginService.getServerConfig();
    }

    /**
     * USBKey登录
     *
     * @param userDto
     * @return
     */
    @PostMapping("certLogin")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.USER_LOGIN)
    public ResultVo<Map<String, Object>> certLogin(@RequestBody UserDto userDto, HttpServletRequest request) throws UnsupportedEncodingException {
        return loginService.certLogin(userDto, request);
    }

    /**
     * 用户名密码登录
     *
     * @param loginDto
     * @return
     */
    @PostMapping("login")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.USER_LOGIN)
    public ResultVo<Object> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        return loginService.login(loginDto, request);
    }

    /**
     * 退出登录
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("logout")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.USER_LOGOUT)
    public ResultVo<Boolean> logout(HttpServletRequest request) {
        return loginService.logout(request);
    }


    /**
     * USEBkey登录是否展示
     *
     * @return true 显示 false 不显示
     */
    @PostMapping("show")
    public ResultVo<Boolean> isShowUsbKey() {
        return loginService.isShowUsbKey();
    }

    /**
     * 只读模式
     * @param loginDto
     * @return
     */
    @PostMapping("readOnly")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.USER_LOGIN)
    public ResultVo<Object> readOnly(@RequestBody LoginDto loginDto) {
        return loginService.readOnly(loginDto);
    }


    /**
     * 下载用户手册
     *
     * @return
     * @throws IOException
     */
    @GetMapping("downloadQuestion")
//    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.LOOK_MANUAl)
    public Boolean downloadQuestion(HttpServletResponse httpServletResponse) throws IOException {
        return loginService.downloadManual(httpServletResponse);
    }
}
