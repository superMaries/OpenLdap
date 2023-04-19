package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.SyncStatusDto;
import cn.ldap.ldap.common.entity.SyncStatus;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface SyncStatusService extends IService<SyncStatus> {
    /**
     * 添加账号url配置
     * @param syncStatusDto
     */
    ResultVo<Object> add(SyncStatusDto syncStatusDto);

    /**
     * 修改
     * @param syncStatusDto
     * @return
     */
    ResultVo<Object> update(SyncStatusDto syncStatusDto);

    ResultVo<Object> mainQuery();

    ResultVo<Object> followQuery();


}
