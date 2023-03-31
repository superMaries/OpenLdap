package cn.ldap.ldap.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class UserInfoUtils {

    /**
     * redis工具
     */
    /**
     * 登录超时时间
     */
    @Value("${login.timeout}")
    private int timeout;


}
