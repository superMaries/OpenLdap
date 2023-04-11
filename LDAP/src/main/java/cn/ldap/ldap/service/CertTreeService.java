package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;

import java.util.List;

/**
 * @title: CertTreeService
 * @Author Wy
 * @Date: 2023/4/11 9:35
 * @Version 1.0
 */
public interface CertTreeService {
    /**
     * 查询目录树接口
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    ResultVo<List<CertTreeVo>> queryCertTree(CertTreeDto treeVo);
    /**
     * 查询节点属性详情
     *
     * @param treeDto 参数
     * @return 返回查询节点属性详情
     */
    ResultVo<List<TreeVo>> queryAttributeInfo(CertTreeDto treeDto);
}
