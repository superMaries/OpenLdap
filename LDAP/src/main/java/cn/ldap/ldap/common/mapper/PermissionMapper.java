package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.dto.PermissionDto;
import cn.ldap.ldap.common.entity.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wy 2022-09-16
 * @version 2.0
 * @Date 2022-09-16
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT id,menu_name,menu_code,icon,parent_id FROM permission WHERE id IN " +
            " (SELECT permission_id FROM role_permission WHERE role_id=#{roleId}) " +
            " ORDER BY id ASC")
    List<PermissionDto> queryPermissionList(Integer roleId);


}
