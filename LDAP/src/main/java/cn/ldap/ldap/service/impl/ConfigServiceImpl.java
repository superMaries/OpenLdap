package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.mapper.ConfigMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  系统配置表
 * @title: ConfigServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 10:30
 * @Version 1.0
 */
@Slf4j
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper,ConfigModel> {
}
