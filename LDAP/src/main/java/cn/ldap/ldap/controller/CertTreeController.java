package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.dto.ParamDto;
import cn.ldap.ldap.common.dto.*;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import cn.ldap.ldap.service.CertTreeService;
import cn.ldap.ldap.service.FileNameService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 目录树接口
 *
 * @title: CertController
 * @Author Wy
 * @Date: 2023/4/11 9:01
 * @Version 1.0
 */
@RestController
@RequestMapping("/tree/")
public class CertTreeController {

    @Resource
    private FileNameService fileNameService;

    @Resource
    private CertTreeService certTreeService;

    /**
     * 查询目录树接口
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @PostMapping("queryCertTree")
    public ResultVo<List<CertTreeVo>> queryCertTree(@RequestBody CertTreeDto treeVo) {
        return certTreeService.queryCertTree(treeVo);
    }

    /**
     * 查询节点属性详情
     *
     * @param treeDto 参数
     * @return 返回查询节点属性详情
     */
    @PostMapping("queryAttributeInfo")
    public ResultVo<List<TreeVo>> queryAttributeInfo(@RequestBody CertTreeDto treeDto) {
        return certTreeService.queryAttributeInfo(treeDto);
    }

    /**
     * 根据条件查询目录树
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @PostMapping("queryTree")
    public ResultVo<Map<String ,Object>> queryTree(@RequestBody CertTreeDto treeVo) {
        return certTreeService.queryTree(treeVo);
    }

    /**
     * 只需要传递 rdn  scope 的值 （ one`：搜索指定的DN及其一级子节点。`sub`：搜索指定的DN及其所有子孙节点。）
     *
     * @param treeVo 参数
     * @return 返回一个Map  其中表示rdn 和num
     */
    @PostMapping("queryTreeRdnOrNum")
    public ResultVo<Map<String, Object>> queryTreeRdnOrNum(@RequestBody CertTreeDto treeVo) {
        return certTreeService.queryTreeRdnOrNum(treeVo);
    }

    /**
     * 删除Ldap
     *
     * @param ldapDto 参数
     * @return true 成功 false 失败
     */
    @PostMapping("del")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.DEL_LDAP)
    public ResultVo<Boolean> delLdapTreByRdn(@RequestBody LdapDto ldapDto) {
        return certTreeService.delLdapTreByRdn(ldapDto);
    }

    /**
     * 编辑属性
     *
     * @param ldapBindTreeDto 参数
     * @return true 成功 false 失败
     */
    @PostMapping("updateLdap")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.EDIT_LDAP_ATTRITE)
    public ResultVo<Boolean> updateLdapBindTree(@RequestBody LdapBindTreeDto ldapBindTreeDto) {
        return certTreeService.updateLdapBindTree(ldapBindTreeDto);
    }

    /**
     * 修改节点名称
     *
     * @param bindTree 参数
     * @return true 成功 false 失败
     */
    @PostMapping("reBind")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.MIDIFY_LDAP_NAME)
    public ResultVo<Boolean> reBIndLdapTree(@RequestBody ReBindTreDto bindTree) {
        return certTreeService.reBIndLdapTree(bindTree);
    }

    /**
     * 查询导出全部
     *
     * @param paramDto
     * @param response
     * @return
     */
    /**
     * 导出
     * @param paramDto
     * @param response
     * @return
     */
    @GetMapping("exportQueryData")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.EXPORT_ALL)
    public Boolean exportQueryData(ParamDto paramDto, HttpServletResponse response) {
        return certTreeService.exportQueryData(paramDto,response);
    }

    /**
     * 查询文件路径
     * @return
     */
    @PostMapping("queryFileName")
    public ResultVo<String> queryFileName() {
        return fileNameService.queryFileName();
    }

    /**
     * 导出LDIF文件
     *
     * @param exportDto
     * @return
     */
    @GetMapping("export")
//    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.EXPORT_LDIF)
    public ResultVo<Boolean> exportLdifByBaseDn(LdifDto exportDto, HttpServletResponse response) {
        return certTreeService.exportLdifByBaseDn(exportDto, response);
    }

    /**
     * 导入LDIF文件
     *
     * @param importDto
     * @return true 成功 false 失败
     */
    @PostMapping("import")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.IMPORT_LDAP)
    public ResultVo<Boolean> importLdifByBaseDn(ImportDto importDto) {
        return certTreeService.importLdifByBaseDn(importDto);
    }

    /**
     * 新增LDAP节点
     *
     * @param createLdapDto 参数
     * @return true 成功 false 失败
     */
    @PostMapping("crate")
    @OperateAnnotation(operateModel = OperateMenuEnum.CATALOGUE_MANAGER, operateType = OperateTypeEnum.ADD_LDAF)
    public ResultVo<Boolean> crateLdap(@RequestBody CreateLdapDto createLdapDto) {
        return certTreeService.crateLdap(createLdapDto);
    }

}
