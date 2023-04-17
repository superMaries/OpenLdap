package cn.ldap.ldap.common.exception;

import com.alibaba.fastjson.JSON;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 返回数据封装
 *
 * @title: ResponseResultConfig
 * @Author Wy
 * @Date: 2023/4/17 11:25
 * @Version 1.0
 */
@RestControllerAdvice
@Slf4j
public class ResponseResultConfig implements HandlerInterceptor, ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 返回数据格式处理.
     *
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return ResultUtil.fail();
        }

        /**
         * 当返回类型为正确类型的时候.
         */
        if (body instanceof ResultVo) {
            return body;
        }

        //String类型需要特殊处理 手动转为json字符串
        if (body instanceof String) {
            return JSON.toJSONString(ResultUtil.success(body));
        }

        return ResultUtil.success(body);
    }

    /**
     * 统一异常处理.
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultVo handler(Exception e) {
        if (e instanceof SysException) {
            SysException sysException = (SysException) e;
            return ResultUtil.fail(sysException);
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            return ResultUtil.fail(ExceptionEnum.REQUEST_WAY_ERROR);
        } else {
            log.error("【系统异常】={}", e);
            return ResultUtil.fail();
        }
    }
}
