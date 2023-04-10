package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.UserAccountModel;
import cn.ldap.ldap.common.entity.UserModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin mapper
 * @title: UserAccountMapper
 * @Author Wy
 * @Date: 2023/4/10 16:10
 * @Version 1.0
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountModel> {
}
