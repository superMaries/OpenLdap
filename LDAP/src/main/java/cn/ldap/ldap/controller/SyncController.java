package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.FromSyncDto;
import cn.ldap.ldap.common.dto.QueryFollowNumDto;
import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.dto.SyncStatusDto;
import cn.ldap.ldap.common.entity.SyncStatus;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SyncService;
import cn.ldap.ldap.service.SyncStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

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

    @Resource
    private SyncStatusService syncStatusService;

    /**
     * 添加/修改主服务的配置信息
     * @param syncDto
     * @return
     */
    @PostMapping("mainSyncConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPDATE_PARAM)
    public ResultVo<Object> mainSyncConfig(@RequestBody SyncDto syncDto) {
        return syncService.syncConfig(syncDto);
    }

    /**
     * 添加/修改从服务的配置信息
     * @param fromSyncDto
     * @return
     */
    @PostMapping("fromSyncConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPDATE_PARAM)
    public ResultVo<Object> fromSyncConfig(@RequestBody FromSyncDto fromSyncDto) {
        return syncService.fromSyncConfig(fromSyncDto);
    }

    /**
     * 添加
     *
     * @param syncDto
     * @return
     */
    @PostMapping("add")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.ADD_DATA)
    public ResultVo<Object> add(@RequestBody SyncStatusDto syncDto) {
        return syncStatusService.add(syncDto);
    }

    /**
     * 修改
     *
     * @param syncDto
     * @return
     */
    @PostMapping("update")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.ADD_DATA)
    public ResultVo<Object> update(@RequestBody SyncStatusDto syncDto) {
        return syncStatusService.update(syncDto);
    }

    /**
     * 删除
     *
     * @param id
     * @return 返回一个boolean值
     */
    @PostMapping("delete/{id}")
    public Boolean deleteById(@PathVariable Integer id) {
        return syncStatusService.removeById(id);
    }

    /**
     * 主服务同步状态节课
     *
     * @return 查询的从服务和主服务对比信息的集合
     */
    @PostMapping("mainQuery")
    public ResultVo<Object> mainQuery() {
        return syncStatusService.mainQuery();
    }

    @PostMapping("followQuery")
    public ResultVo<Object> followQuery() {
        return syncStatusService.followQuery();
    }

    @PostMapping("queryServiceConfig")
    public ResultVo<Map<String, String>> queryServiceConfig() {
        return syncStatusService.queryServiceConfig();
    }

    @PostMapping("queryFollowNum")
    public ResultVo<SyncStatus> queryFollowNum(@RequestBody QueryFollowNumDto queryFollowNumDto){
        return syncStatusService.queryFollowNum(queryFollowNumDto);
    }


}


