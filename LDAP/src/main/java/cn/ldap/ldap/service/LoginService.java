package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.PermissionDto;
import cn.ldap.ldap.common.dto.UserDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    Map<String,String> getVersion();

    /**
     * 下载用户手册接口
     * @param
     * @return
     */
    byte[] downloadManual() throws IOException;

    /**
     * 查看菜单
     * @param roleId
     * @return
     */
    List<PermissionDto> queryMenus(Integer roleId);

    /**
     * 是否初始化
     * @return
     */
    Integer whetherInit();

    /**
     * 获取服务模式
     * @return
     */
    Integer getServerConfig();

    /**
     * USBKey登录
     * @param userDto
     * @return
     */
    Map<String,Object> certLogin(UserDto userDto);

    /**
     * 账号密码登录
     * @param loginDto
     * @return
     */
    String login(LoginDto loginDto);

    /**
     * 退出登录
     * @param request
     * @param userDto
     * @return
     */
    boolean logout(HttpServletRequest request);


}
