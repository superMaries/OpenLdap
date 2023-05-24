package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.LdapAccountDto;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.LdapAccountVo;
import cn.ldap.ldap.common.vo.PageVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapAccountService;
import cn.ldap.ldap.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * LDAP 账号信息
 *
 * @title: LdapAccountController
 * @Author Wy
 * @Date: 2023/5/8 9:24
 * @Version 1.0
 */
@RestController
@RequestMapping("ldap/account/")
@Slf4j
public class LdapAccountController {
    @Resource
    private LdapAccountService ldapAccountService;

    /**
     * 获取查询权限
     */
    @PostMapping("query/auth")
    public ResultVo<Map<Integer, String>> queryAuth() {
        return ldapAccountService.queryAuth();
    }

    /**
     * 查询账号
     * @param pageVo 分页条件
     */
    @PostMapping("query")
    public  ResultVo< Map<String, Object>> queryLdapAccount(@RequestBody  PageVo pageVo) {
        return ldapAccountService.queryLdapAccount(pageVo);
    }

    /**
     * 添加账号
     */
    @PostMapping("add")
    @OperateAnnotation(operateModel = OperateMenuEnum.LDAP_ACCOUNT, operateType = OperateTypeEnum.ACCOUNT_ADD)
    public ResultVo<Boolean> addLdapAccount(@RequestBody LdapAccountDto ldapAccountDto) {
        return ldapAccountService.addLdapAccount(ldapAccountDto);
    }

    /**
     * 编辑账号
     */
    @PostMapping("edit")
    @OperateAnnotation(operateModel = OperateMenuEnum.LDAP_ACCOUNT, operateType = OperateTypeEnum.ACCOUNT_UPDATE_PASSWORD)
    public ResultVo<Boolean> editLdapAccount(@RequestBody LdapAccountDto ldapAccountDto) {
        return ldapAccountService.editLdapAccount(ldapAccountDto);
    }

    /**
     * 删除
     */
    @PostMapping("del")
    @OperateAnnotation(operateModel = OperateMenuEnum.LDAP_ACCOUNT, operateType = OperateTypeEnum.ACCOUNT_DELETE)
    public ResultVo<Boolean> delLdapAccount(@RequestBody LdapAccountDto ldapAccountDto) {
        return ldapAccountService.delLdapAccount(ldapAccountDto);
    }
}
