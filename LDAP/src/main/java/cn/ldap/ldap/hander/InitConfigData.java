package cn.ldap.ldap.hander;

import cn.ldap.ldap.common.entity.KeyModel;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.KeyMapper;
import cn.ldap.ldap.common.util.StaticValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Resource;
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


    @Value("${filePath.sync}")
    private Integer sync;

    @Value("${filePath.serviceType}")
    private Integer configServiceType;

    @Value("${filePath.usbKey}")
    private Integer configUsbKey;


    private static String privateKey = "";
    private static String publicKey = "";

    @Resource
    private KeyMapper keyMapper;

    /**
     * 是否同步
     * 0 不展示同步
     * 1 展示同步
     */
    private static Integer isSync = 0;
    /**
     * 0, "主服务器"
     * 1, "从服务器"
     */
    private static Integer serviceType = 0;

    private static Integer usbKey = 0;

    /**
     * 0, "主服务器"
     * 1, "从服务器"
     *
     * @return 1|0
     */
    public static Integer getServiceType() {
        return serviceType;
    }

    public static Integer getUsbKey() {
        return usbKey;
    }

    /**
     * 获取公钥
     *
     * @return 公钥
     */
    public static String getPublicKey() {
        return publicKey;
    }

    public static String getPrivateKey() {
        return privateKey;
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
        serviceType = configServiceType;
        isSync = sync;
        usbKey = configUsbKey;


        List<KeyModel> keyModels = keyMapper.selectList(null);
        if (ObjectUtils.isEmpty(keyModels)) {
            log.error("系统配置文件错误。，。公私钥未配置");
            throw new SysException("系统配置文件错误。，。公私钥未配置");
        }
        KeyModel keyModel = keyModels.get(StaticValue.SPLIT_COUNT);
        publicKey = keyModel.getPublicKey();
        privateKey = keyModel.getPrivateKey();
    }
}
