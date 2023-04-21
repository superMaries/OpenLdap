package cn.ldap.ldap.service;

import cn.ldap.ldap.common.entity.SSLConfig;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface SSLConfigService extends IService<SSLConfig> {

    ResultVo<SSLConfig> queryServerConfig();
}
