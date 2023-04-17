package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.*;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import cn.ldap.ldap.service.CertTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.basic.BasicViewportUI;
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
     * 删除Ldap
     * 删除节点必须要先删除子节点
     *
     * @param ldapDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> delLdapTreByRdn(LdapDto ldapDto) {
        boolean result = LdapUtil.delLdapTreByRdn(ldapTemplate, ldapDto, ldapSearchFilter);
        return ResultUtil.success(result);
    }

    /**
     * 编辑属性
     *
     * @param ldapBindTreeDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> updateLdapBindTree(LdapBindTreeDto ldapBindTreeDto) {
        boolean result = LdapUtil.updateLdapBindTree(ldapTemplate, ldapBindTreeDto, ldapSearchFilter);
        return ResultUtil.success(true);
    }

    /**
     * 修改节点名称
     *
     * @param bindTree 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> reBIndLdapTree(ReBindTreDto bindTree) {
        boolean b = LdapUtil.reBIndLdapTree(ldapTemplate, bindTree, ldapSearchFilter);
        return ResultUtil.success(b);
    }

    /**
     * 导出LDIF文件
     *
     * @param exportDto
     * @return
     */
    @Override
    public ResultVo<Boolean> exportLdifByBaseDn(LdifDto exportDto, HttpServletResponse response) {
        if (ObjectUtils.isEmpty(exportDto)
                || ObjectUtils.isEmpty(exportDto.getBaseDN())
                || ObjectUtils.isEmpty(exportDto.getScope())
                || ObjectUtils.isEmpty(exportDto.getExportType())
                || ObjectUtils.isEmpty(exportDto.getExportFilePath())) {
            log.error("缺少参数:{}", exportDto);
            throw new SysException(ExceptionEnum.PARAM_ERROR);
        }
        //设置默认值
        exportDto.setBaseFilter(ldapSearchFilter);
        Boolean result = LdapUtil.exportLdifFile(ldapTemplate, exportDto, response);
        return ResultUtil.success(result);
    }

    /**
     * 导入LDIF文件
     *
     * @param exportDto
     * @return
     */
    @Override
    public ResultVo<Boolean> importLdifByBaseDn(LdifDto exportDto, HttpServletResponse response) {
        return null;
    }
}
