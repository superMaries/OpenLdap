package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import cn.ldap.ldap.service.CertTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: CertTreeServiceImpl
 * @Author Wy
 * @Date: 2023/4/11 9:35
 * @Version 1.0
 */
@Service
@Slf4j
public class CertTreeServiceImpl implements CertTreeService {

    @Resource
    private LdapTemplate ldapTemplate;
    @Value("${ldap.searchBase}")
    private String ldapSearchBase;
    @Value("${ldap.searchFilter}")
    private String ldapSearchFilter;

    /**
     * 查询目录树接口
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @Override
    public ResultVo<List<CertTreeVo>> queryCertTree(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            treeVo = new CertTreeDto();
            treeVo.setBaseDN(ldapSearchBase);
            treeVo.setBaseDN(ldapSearchFilter);
        }
        if (ObjectUtils.isEmpty(treeVo.getBaseDN())) {
            treeVo.setBaseDN(ldapSearchBase);
            treeVo.setFilter(ldapSearchFilter);
        }
        if (ObjectUtils.isEmpty(treeVo) || ObjectUtils.isEmpty(treeVo.getBaseDN())) {
            log.info("参数异常；{}", ExceptionEnum.PARAM_ERROR);
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(ldapTemplate, treeVo.getFilter(), treeVo.getBaseDN(), treeVo.getScope(), treeVo.getPageSize());
        return ResultUtil.success(listResultVo);
    }

    /**
     * 查询节点属性详情
     *
     * @param treeDto 参数
     * @return 返回查询节点属性详情
     */
    @Override
    public ResultVo<List<TreeVo>> queryAttributeInfo(CertTreeDto treeDto) {
        if (ObjectUtils.isEmpty(treeDto)
                || ObjectUtils.isEmpty(treeDto.getBaseDN())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }

        List<TreeVo> treeVos = LdapUtil.queryAttributeInfo(ldapTemplate, treeDto.getBaseDN(), treeDto.isReturnAttr(), treeDto.getAttribute());
        return ResultUtil.success(treeVos);
    }

    /**
     * 根据条件查询目录树
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @Override
    public ResultVo<List<CertTreeVo>> queryTree(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(ldapTemplate, treeVo.getFilter(), treeVo.getBaseDN(), treeVo.getScope(), treeVo.getPageSize());
        return ResultUtil.success(listResultVo);
    }

    /**
     * 只需要传递 rdn  scope 的值 （ one`：搜索指定的DN及其一级子节点。`sub`：搜索指定的DN及其所有子孙节点。）
     *
     * @param treeVo 参数
     * @return 返回一个Map  其中表示rdn 和num
     */
    @Override
    public ResultVo<Map<String, Object>> queryTreeRdnOrNum(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        Map<String, Object> map = new HashMap<>();
        map = LdapUtil.queryTreeRdnOrNum(map, ldapTemplate, treeVo.getScope(), treeVo.getBaseDN(), treeVo.getFilter());
        return ResultUtil.success(map);
    }

    /**
     * 测试
     */
    private void querySchema() {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        try {
            NamingEnumeration<SearchResult> results = ctx.search("cn=subschema", "(objectClass=*)", searchControls);
            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attrs = searchResult.getAttributes();
                Attribute objectClasses = attrs.get("objectClasses");
                if (objectClasses != null) {
                    NamingEnumeration<?> all = objectClasses.getAll();
                    while (all.hasMore()) {
                        String objectClass = (String) all.next();
                        System.out.println(objectClass);
                    }
                }
            }

        } catch (NamingException e) {
            throw new RuntimeException(e);
        }


    }
}
