package cn.ldap.ldap.service;

import cn.ldap.ldap.common.vo.ObjectDataDto;

import java.util.List;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface ObjectClassInformationService {

    List<ObjectDataDto> queryObjectAndAttribute();
}
