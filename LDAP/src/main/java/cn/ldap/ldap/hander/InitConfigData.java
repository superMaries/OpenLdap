package cn.ldap.ldap.hander;

import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.mapper.ConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.List;

/**
 * 项目启动获取参数配置
 *
 * @title: InitConfigData
 * @Author Wy
 * @Date: 2023/4/10 16:23
 * @Version 1.0
 */
@Slf4j
@Component
public class InitConfigData implements InitializingBean, ServletContextAware {

    /**
     * 是否展示同步
     */
    private final static String SYNC = "SYNC";
    /**
     * 根据该值获取 是主服务还是从服务
     */
    private final static String SERVICE = "SERVICE";
    @Resource
    private ConfigMapper configMapper;
    /**
     * 是否同步
     */
    private static Integer isSync = 0;
    /**
     * 0, "主服务器"
     * 1, "从服务器"
     */
    private  static  Integer serviceType = 0;

    /**
     * 0, "主服务器"
     * 1, "从服务器"
     *
     * @return 1|0
     */
    public static Integer getServiceType() {
        return serviceType;
    }

    /**
     * 是否同步
     *
     * @return 1|0
     */
    public static Integer getIsSync() {
        return isSync;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        //对数据进行获取
        List<ConfigModel> configModels = configMapper.selectList(null);
        for (ConfigModel configModel : configModels) {
            if (SERVICE.equals(configModel.getCode())) {
                isSync = configModel.getServiceType();
            } else {
                serviceType = configModel.getServiceType();
            }
        }
    }
}
