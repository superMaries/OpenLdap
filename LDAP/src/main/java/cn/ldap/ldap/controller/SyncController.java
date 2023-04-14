package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.FromSyncDto;
import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/sync/")
public class SyncController {

    @Resource
    private SyncService syncService;

    @PostMapping("mainSyncConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<Object> mainSyncConfig(@RequestBody SyncDto syncDto) {
        return syncService.syncConfig(syncDto);
    }

    @PostMapping("fromSyncConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<Object> fromSyncConfig(@RequestBody FromSyncDto fromSyncDto) {
        return syncService.fromSyncConfig(fromSyncDto);
    }
}
