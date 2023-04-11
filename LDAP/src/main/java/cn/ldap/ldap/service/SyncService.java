package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.vo.ResultVo;

public interface SyncService {


    ResultVo<Object> syncConfig(SyncDto syncDto);
}
