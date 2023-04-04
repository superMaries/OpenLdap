package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.mapper.PermissionMapper;
import cn.ldap.ldap.service.PermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
