package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.vo.ResultVo;

import java.util.Map;

/**
 * @title: UserService
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public interface UserService {
    ResultVo isInit();
    ResultVo importConfig(UserDto userDto);
    ResultVo importAdminKey(UserDto userDto);
}
