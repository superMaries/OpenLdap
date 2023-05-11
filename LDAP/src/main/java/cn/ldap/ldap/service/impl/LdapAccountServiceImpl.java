package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.config.LdapConfig;
import cn.ldap.ldap.common.dto.LdapAccountDto;
import cn.ldap.ldap.common.dto.LdapBindTreeDto;
import cn.ldap.ldap.common.enums.LdapAccuntAuthEnum;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.LdapAccountVo;
import cn.ldap.ldap.common.vo.PageVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import cn.ldap.ldap.service.LdapAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.*;

/**
 * @title: LdapAccountServiceImpl
 * @Author Wy
 * @Date: 2023/5/8 9:59
 * @Version 1.0
 */
@Service
@Slf4j
public class LdapAccountServiceImpl implements LdapAccountService {
    @Resource
    private LdapTemplate ldapTemplate;

    /**
     * 设置过滤器
     */
    @Value("${ldap.searchFilter}")
    private String ldapSearchFilter;
    /**
     * 设置要查询的基本DN
     */
    @Value("${ldap.searchBase}")
    private String ldapSearchBase;

    @Value("${filePath.configPath}")
    private String filePath;


//    public LdapAccountServiceImpl() {
//        newLdapTemplate = fromPool();
//    }

    /**
     * 获取查询权限
     */
    @Override
    public ResultVo<Map<Integer, String>> queryAuth() {
        return ResultUtil.success(LdapAccuntAuthEnum.getLdapAccountData());
    }

    /**
     * 查询账号
     *
     * @param pageVo 分页条件
     */
    @Override
    public ResultVo<Map<String, Object>> queryLdapAccount(PageVo pageVo) {
        List<LdapAccountVo> ldapList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //查询账号
        LdapTemplate newLdapTemplate = fromPool();
        ldapList = LdapUtil.queryLdapAccount(newLdapTemplate, ldapSearchFilter, ldapSearchBase);
        //总数
        long count = ldapList.stream().count();
        //开始的数量
        Integer startNum = Math.toIntExact((pageVo.getPageIndex() - 1) * pageVo.getPageSize());
        //结束的数量
        Integer endNum = Math.toIntExact(pageVo.getPageIndex() * pageVo.getPageSize());

        if (startNum < count) {
            endNum = Math.toIntExact(Math.min(endNum, count));
            ldapList = ldapList.subList(startNum, endNum);
        } else {
            return ResultUtil.success(Collections.emptyList());
        }
        map.put(StaticValue.TOTAL, count);
        map.put(StaticValue.DATA, ldapList);
        return ResultUtil.success(map);
    }

    /**
     * 添加账号
     */
    @Override
    public ResultVo<Boolean> addLdapAccount(LdapAccountDto ldapAccountDto) {
        Boolean result = null;
        try {
            LdapTemplate newLdapTemplate = fromPool();
            result = LdapUtil.addLdapAccount(newLdapTemplate, ldapSearchFilter, ldapSearchBase, ldapAccountDto,filePath);


        } catch (NamingException e) {
            log.error(e.getMessage());
            result = false;
        }
        return ResultUtil.success(result);
    }

    /**
     * 删除
     */
    @Override
    public ResultVo<Boolean> delLdapAccount(LdapAccountDto ldapAccountDto) {
        LdapTemplate newLdapTemplate = fromPool();
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        LdapUtil.queryChildRdn(ldapAccountDto.getAccount(), ldapSearchFilter, ctx);
        return ResultUtil.success(StaticValue.TRUE);
    }

    /**
     * 编辑账号
     * 只修改密码和权限
     */
    @Override
    public ResultVo<Boolean> editLdapAccount(LdapAccountDto ldapAccountDto) {
        LdapBindTreeDto bindTreeDto = new LdapBindTreeDto();
        bindTreeDto.setRdn(ldapAccountDto.getAccount());
        TreeVo treeVo = new TreeVo();
        treeVo.setKey("userPassword");
        treeVo.setValue(ldapAccountDto.getPwd());
        treeVo.setTitle(ldapAccountDto.getPwd());
        List<TreeVo> vos = new ArrayList<>();
        vos.add(treeVo);
        bindTreeDto.setAttributes(vos);
        boolean b = LdapUtil.updateLdapBindTree(ldapTemplate, bindTreeDto, ldapSearchFilter);

        return ResultUtil.success(b);
    }


    public LdapTemplate fromPool() {
        // 从连接池获取PooledContextSource对象
        PooledContextSource pooledContextSource = (PooledContextSource) ldapTemplate.getContextSource();
// 将PooledContextSource转换为LdapContextSource
        LdapContextSource ldapContextSource = (LdapContextSource) pooledContextSource.getContextSource();
// 创建新的LdapTemplate对象
        LdapTemplate newLdapTemplate = new LdapTemplate(ldapContextSource);
        return newLdapTemplate;
    }

}
