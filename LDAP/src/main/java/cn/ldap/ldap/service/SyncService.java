package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.FromSyncDto;
import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.vo.ResultVo;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface SyncService {


    ResultVo<Object> syncConfig(SyncDto syncDto);

    ResultVo<Object> fromSyncConfig(FromSyncDto fromSyncDto);
}
