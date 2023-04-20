package cn.ldap.ldap.common.util;

import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.vo.LoginResultVo;
import org.apache.catalina.connector.RequestFacade;
import org.omg.CORBA.SystemException;
import org.springframework.util.ObjectUtils;


import javax.servlet.ServletRequest;

/**
 * @title: SessionUtil
 * @Author Wy
 * @Date: 2023/4/4 14:05
 * @Version 1.0
 */
public class SessionUtil {
    private static final String AUTHORIZATION = "auth";

    public static LoginResultVo getUserInfo(ServletRequest requests) {
        LoginResultVo loginResultVo = null;
        try {

            loginResultVo = (LoginResultVo) ((RequestFacade) requests).getSession().getAttribute(AUTHORIZATION);
            String auth = ((RequestFacade) requests).getHeader(AUTHORIZATION);
            if (ObjectUtils.isEmpty(auth)) {
                throw new SysException(ExceptionEnum.USER_NOT_LOGIN);
            }
            if (auth.equals(loginResultVo.getAuthorization())) {
                throw new SysException(ExceptionEnum.USER_NOT_LOGIN);
            }
        } catch (Exception e) {
            //用户未登录
            throw new SysException(ExceptionEnum.USER_NOT_LOGIN);
        }
        return loginResultVo;
    }
}
