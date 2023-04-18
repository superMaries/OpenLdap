package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.dto.ParamDto;
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
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
     * 换行
     */
    private static final String FEED = "\n";

    private static final String LAST = ".ldif";

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

    @Override
    public Boolean exportQueryData(ParamDto paramDto, HttpServletResponse response) {
        String fileName = paramDto.getFileName()+LAST;
        if (ObjectUtils.isEmpty(paramDto)) {
            return false;
        }
        List<String> writeData = queryData(paramDto);
        exportToLdif(writeData,fileName);
        // 下载ldif文件
        Path file = Paths.get(fileName);
        if (Files.exists(file)) {
            response.setContentType("application/ldif");
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            try {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<String> queryData(ParamDto paramDto){
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(ldapTemplate, paramDto.getFilter(), paramDto.getBaseDN(), paramDto.getScope(), paramDto.getPageSize());
        List<String> strings = new ArrayList<>();
        for (CertTreeVo certTreeVo : listResultVo) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            List<TreeVo> treeVos = LdapUtil.queryAttributeBytesInfo(ldapTemplate, certTreeVo.getRdn(), paramDto.isReturnAttr(), paramDto.getAttribute());
            map.put("dn:",certTreeVo.getRdn());
            strings.add("dn:"+certTreeVo.getRdn());
            for (TreeVo treeVo : treeVos) {
                map.put(treeVo.getKey(),treeVo.getValue());
                strings.add(treeVo.getKey()+":"+treeVo.getValue());
            }
            strings.add(FEED);
        }
        return strings;
    }


    public static void exportToLdif(List<String> data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String line : data) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
