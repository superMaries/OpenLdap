package cn.ldap.ldap.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ldap 过滤器
 *
 * @title: LdapFilter
 * @Author Wy
 * @Date: 2023/4/12 9:43
 * @Version 1.0
 */
@Configuration
public class LdapFilter implements WebMvcConfigurer {

    /**
     * 添加拦截地址和开发地址
     * 只有登录页面的接口不需要登录可以访问 其余的接口必须带token  否则就不可以进行访问
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //是否进行了登录
        InterceptorRegistration registration = registry.addInterceptor(new HandleConfig());
        registration.addPathPatterns("/**")
                .excludePathPatterns("/login/**")
                .addPathPatterns("/config/**")
                .addPathPatterns("/login/logout");
    }
}
