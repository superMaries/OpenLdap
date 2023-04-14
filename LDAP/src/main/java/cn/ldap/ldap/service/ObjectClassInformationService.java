package cn.ldap.ldap.service;

import cn.ldap.ldap.common.vo.ObjectDataDto;

import java.util.List;

public interface ObjectClassInformationService {

    List<ObjectDataDto> queryObjectAndAttribute();
}
