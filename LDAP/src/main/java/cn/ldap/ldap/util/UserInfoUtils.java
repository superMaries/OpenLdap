package cn.ldap.ldap.util;

import cn.hutool.json.JSONUtil;
import cn.ldap.ldap.common.entity.Information;
import cn.ldap.ldap.common.redis.RedisUtils;
import cn.ldap.ldap.common.vo.UserRedisInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


@Component
public class UserInfoUtils {

    /**
     * redis工具
     */
    @Autowired
    private RedisUtils redisUtils;
    /**
     * 登录超时时间
     */
    @Value("${login.timeout}")
    private int timeout;

}
