package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.LdapAccountDto;
import cn.ldap.ldap.common.vo.LdapAccountVo;
import cn.ldap.ldap.common.vo.PageVo;
import cn.ldap.ldap.common.vo.ResultVo;

import java.util.List;
import java.util.Map;

public interface LdapAccountService {
    /**
     * 获取查询权限
     */
    ResultVo<Map<Integer, String>> queryAuth();
    /**
     * 查询账号
     * @param pageVo 分页条件
     */

    ResultVo< Map<String, Object>> queryLdapAccount(PageVo pageVo);
    /**
     * 添加账号
     */
    ResultVo<Boolean> addLdapAccount(LdapAccountDto ldapAccountDto);
    /**
     * 删除密码
     */
    ResultVo<Boolean> delLdapAccount(LdapAccountDto ldapAccountDto);
    /**
     * 编辑账号
     */
    ResultVo<Boolean> editLdapAccount(LdapAccountDto ldapAccountDto);
}
