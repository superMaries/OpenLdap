package cn.ldap.ldap.common.config;

import cn.ldap.ldap.common.enums.ExceptionEnum;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import sun.misc.CharacterEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @title: HandleConfig
 * @Author Wy
 * @Date: 2023/4/12 10:05
 * @Version 1.0
 */
@Component
@Slf4j
public class HandleConfig implements HandlerInterceptor {
    private static String TOKEN_SECRET_KEY = "ldapKey";
    private static String BY_TOKEN = "auth";
    private static String CONTENT_TYPE = "application/json;charset=utf-8";
    private final static String SIGN = "sign";
    private final static String ORIGN = "orgin";

    private static final String GET="GET";

    private static final String TOKEN="token";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = "";
        if (request.getMethod().equals(GET)){
            token = request.getParameter(TOKEN);
        }else {
            //获取头中的token
            token = request.getHeader(BY_TOKEN);
        }

        if (ObjectUtils.isEmpty(token)) {
            response.setCharacterEncoding(Charsets.UTF_8.toString());
            response.setContentType(CONTENT_TYPE);
            response.setStatus(ExceptionEnum.USER_NOT_LOGIN.getCode());
            return false;
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET_KEY)).build();
        try {
            verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error(e.getMessage());
            response.setCharacterEncoding(Charsets.UTF_8.toString());
            response.setContentType(CONTENT_TYPE);
            response.setStatus(ExceptionEnum.USER_NOT_LOGIN.getCode());
            return false;
        }
        return true;
    }

}
