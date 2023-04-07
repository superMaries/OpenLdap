package cn.ldap.ldap.common.util;

import cn.hutool.json.JSONUtil;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.vo.LoginResultVo;


import javax.servlet.ServletRequest;

/**
 * @title: SessionUtil
 * @Author Wy
 * @Date: 2023/4/4 14:05
 * @Version 1.0
 */
public class SessionUtil {
    private static final String AUTHORIZATION = "Authorization";

    public static LoginResultVo getUserInfo(ServletRequest requests) {
        LoginResultVo loginResultVo=null;
        try {
            loginResultVo = (LoginResultVo) requests.getAttribute(AUTHORIZATION);
        } catch (Exception e) {
            //用户未登录
            throw new SystemException(ExceptionEnum.USER_NOT_LOGIN);
        }
        return loginResultVo;
    }
}
