package cn.ldap.ldap.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.ldap.ldap.common.entity.SSLConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.mapper.SSLConfigMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SSLConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class SSLConfigServiceImpl extends ServiceImpl<SSLConfigMapper, SSLConfig> implements SSLConfigService {
    @Override
    public ResultVo<SSLConfig> queryServerConfig() {
        SSLConfig ssl = getOne(null);
        if (ObjectUtil.isEmpty(ssl)){
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }else {
            return ResultUtil.success(ssl);
        }

    }
}
