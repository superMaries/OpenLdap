package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.ConfigModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @title: CongfigMapper
 * @Author Wy
 * @Date: 2023/3/31 10:30
 * @Version 1.0
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigModel> {
}
