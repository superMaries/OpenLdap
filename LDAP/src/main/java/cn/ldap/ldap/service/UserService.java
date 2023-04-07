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
    /**
     * 是否初始化
     * @return 返回true 不需要初始化 否则 初始化
     */
    ResultVo<Map<String, Object> > isInit();

    /**
     *
     * @param userDto 导入配置实体
     * @return ture 成功
     */
    ResultVo<Boolean> importConfig(UserDto userDto);
    /**
     *
     * @param userDto 导入配置实体
     *  @return ture 成功
     */
    ResultVo<Boolean> importAdminKey(UserDto userDto);
}
