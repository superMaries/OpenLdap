package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.UserModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @title: UserMappder
 * @Author Wy
 * @Date: 2023/3/31 9:30
 * @Version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserModel> {
}
