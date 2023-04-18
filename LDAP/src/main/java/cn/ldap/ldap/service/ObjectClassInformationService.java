package cn.ldap.ldap.service;

import cn.ldap.ldap.common.vo.ObjectDataDto;
import cn.ldap.ldap.common.vo.ResultVo;

import java.util.List;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface ObjectClassInformationService {

    /**
     * 查询ObjectClass和属性接
     * @return
     */
    ResultVo<List<ObjectDataDto>> queryObjectAndAttribute();
}
