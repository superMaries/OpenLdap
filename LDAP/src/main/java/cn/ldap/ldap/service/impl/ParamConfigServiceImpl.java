package cn.ldap.ldap.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.ldap.ldap.common.entity.ParamConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.mapper.ParamConfigMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.ParamConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class ParamConfigServiceImpl extends ServiceImpl<ParamConfigMapper, ParamConfig> implements ParamConfigService{

    @Override
    public ResultVo<ParamConfig> queryParamConfig() {
        ParamConfig one = getOne(null);
        if (ObjectUtils.isEmpty(one)){
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }else {
            return ResultUtil.success(one);
        }
    }
}
