package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.UserDto;

import java.util.Map;

/**
 * @title: UserService
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public interface UserService {
    Map<String,Object> isInit();
    boolean importConfig(UserDto userDto);
    boolean importAdminKey(UserDto userDto);
}
