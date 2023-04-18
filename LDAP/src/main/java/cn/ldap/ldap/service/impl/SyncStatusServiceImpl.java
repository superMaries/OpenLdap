package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.SyncStatusDto;
import cn.ldap.ldap.common.entity.SyncStatus;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.mapper.SyncStatusMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SyncStatusService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class SyncStatusServiceImpl extends ServiceImpl<SyncStatusMapper, SyncStatus> implements SyncStatusService {

    /**
     * 添加从服务配置信息
     *
     * @param syncStatusDto
     */
    @Override
    public ResultVo<Object> add(SyncStatusDto syncStatusDto) {
        if (ObjectUtils.isEmpty(syncStatusDto)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_EMPTY);
        }
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setFollowServerIp(syncStatusDto.getUrl());
        syncStatus.setAccount(syncStatusDto.getAccount());
        syncStatus.setPassword(syncStatusDto.getPassword());
        syncStatus.setSyncPoint(syncStatus.getSyncPoint());
        syncStatus.setCreateTime(new Date().toString());
        save(syncStatus);
        return ResultUtil.success();
    }

    /**
     * 修改连接信息
     *
     * @param syncStatusDto
     * @return
     */
    @Override
    public ResultVo<Object> update(SyncStatusDto syncStatusDto) {
        if (ObjectUtils.isEmpty(syncStatusDto.getUrl()) || ObjectUtils.isEmpty(syncStatusDto.getAccount())
                || ObjectUtils.isEmpty(syncStatusDto.getPassword()) || ObjectUtils.isEmpty(syncStatusDto.getSyncPoint())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        QueryWrapper<SyncStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SyncStatus::getFollowServerIp, syncStatusDto.getUrl());
        SyncStatus one = getOne(queryWrapper);
        if (ObjectUtils.isEmpty(one)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }
        one.setSyncPoint(syncStatusDto.getSyncPoint());
        one.setAccount(syncStatusDto.getAccount());
        one.setPassword(syncStatusDto.getPassword());
        one.setFollowServerIp(syncStatusDto.getUrl());
        updateById(one);
        return ResultUtil.success();
    }
}
