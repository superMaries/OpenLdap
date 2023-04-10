package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.vo.ResultVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface LoginService {
    /**
     * 下载客户端工具
     * @param httpServletResponse
     * @return
     */
    Boolean downClientTool(HttpServletResponse httpServletResponse);

    /**
     * 获取版本号
     * @return
     */
    ResultVo<Map<String,String>> getVersion();

    /**
     * 下载用户手册接口
     * @param
     * @return
     */
    byte[] downloadManual();

    /**
     * 查看菜单
     * @param
     * @return
     */
    List<Permission> queryMenus();

    /**
     * 是否初始化
     * @return
     */
    ResultVo<String> whetherInit();

    /**
     * 获取服务模式
     * @return
     */
    ResultVo<String> getServerConfig();

    /**
     * USBKey登录
     * @param userDto
     * @return
     */
    ResultVo<Map<String, Object>> certLogin(UserDto userDto,HttpServletRequest  request);

    /**
     * 账号密码登录
     * @param loginDto
     * @return
     */
    ResultVo<Object> login(LoginDto loginDto,HttpServletRequest  request);

    /**
     * 退出登录
     * @param request
     * @param
     * @return
     */
    Boolean logout(HttpServletRequest request);


}
