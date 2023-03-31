package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.OperationLogModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @title: OperationMapper
 * @Author Wy
 * @Date: 2023/3/31 16:04
 * @Version 1.0
 */
@Mapper
public interface OperationMapper extends BaseMapper<OperationLogModel> {
}
