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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
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
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(ldapTemplate, treeVo.getFilter(), treeVo.getBaseDN(),
                treeVo.getScope(), treeVo.getPageSize(), treeVo.getPage(),null);
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
    public ResultVo<Map<String, Object>> queryTree(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        Map<String, Object> map=new HashMap<>();
         LdapUtil.queryCertTree(ldapTemplate, treeVo.getFilter(), treeVo.getBaseDN(),
                treeVo.getScope(), treeVo.getPageSize(), treeVo.getPage(),map);


        return ResultUtil.success(map);
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
        log.info("导出参数为:{}", exportDto);
        if (ObjectUtils.isEmpty(exportDto)
                || ObjectUtils.isEmpty(exportDto.getBaseDN())
                || ObjectUtils.isEmpty(exportDto.getScope())
                || ObjectUtils.isEmpty(exportDto.getExportType())
                ) {
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
     * @param importDto 上传的文件
     * @return 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> importLdifByBaseDn(ImportDto importDto) {
        log.info("恢复的方法参数为{}", importDto);
        //判断参数是否为空
        if (ObjectUtils.isEmpty(importDto)) {
            log.error("参数为空");
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        //获取上传的文件
        MultipartFile file = importDto.getFile();
        //判断上传文件为空
        if (file.isEmpty()) {
            log.error("文件为空");
            throw new SysException(ExceptionEnum.PARAM_ERROR);
        }
        //获取文件名称
        String name = file.getName();
        //对上传的文件进行解析
        boolean result = LdapUtil.importLap(ldapTemplate, file, name, importDto.getType());
        return ResultUtil.success(result);
    }

    /**
     * 新增LDAP节点
     *
     * @param createLdapDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> crateLdap(CreateLdapDto createLdapDto) {
        log.info("新增LDAP节点{}", createLdapDto);
        if (ObjectUtils.isEmpty(createLdapDto)
                || ObjectUtils.isEmpty(createLdapDto.getRdn())) {
            log.error("参数为空");
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        boolean result = LdapUtil.crateLdap(ldapTemplate, createLdapDto);
        return ResultUtil.success(result);
    }

    @Override
    public Boolean exportQueryData(ParamDto paramDto, HttpServletResponse response) {
        String fileName = paramDto.getFileName();
        if (StaticValue.TRUE == paramDto.getWebOrFile()) {
            fileName = paramDto.getFileName() + LAST;
        } else {
            if (!fileName.endsWith(StaticValue.LDIF)) {
                fileName += StaticValue.LDIF;
            }
        }
        if (ObjectUtils.isEmpty(paramDto)) {
            return false;
        }
        //获取查询的数据
        List<String> writeData = queryData(paramDto);
        exportToLdif(writeData, fileName);
        if (paramDto.getWebOrFile().equals(StaticValue.TRUE)) {
            // 下载ldif文件
            Path file = Paths.get(fileName);
            if (Files.exists(file)) {
                response.setContentType("application/ldif");
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
                try {
                    Files.copy(file, response.getOutputStream());
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //下载到本地
            Path file = Paths.get(fileName);
            //下载到服务器。判断文件是否存在 不存在就创建 不需要返回下的文件
            if (!Files.exists(file)) {
                //不存在
                try {
                    Files.createFile(file);
                    return true;
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new SysException(ExceptionEnum.FILE_IO_ERROR);
                }
            }
        }
        return true;
    }

    public List<String> queryData(ParamDto paramDto) {
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(ldapTemplate, paramDto.getFilter(), paramDto.getBaseDN(),
                paramDto.getScope(), paramDto.getPageSize(), paramDto.getPage(),null);
        List<String> strings = new ArrayList<>();
        for (CertTreeVo certTreeVo : listResultVo) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            List<TreeVo> treeVos = LdapUtil.queryAttributeBytesInfo(ldapTemplate, certTreeVo.getRdn(), paramDto.isReturnAttr(), paramDto.getAttribute());
            map.put("dn:", certTreeVo.getRdn());
            strings.add("dn:" + certTreeVo.getRdn());
            for (TreeVo treeVo : treeVos) {
                map.put(treeVo.getKey(), treeVo.getValue());
                strings.add(treeVo.getKey() + ":" + treeVo.getValue());
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
