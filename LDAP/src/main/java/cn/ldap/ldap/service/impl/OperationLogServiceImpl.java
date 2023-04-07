package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.LogDto;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.OperationMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.OperationLogService;
import cn.ldap.ldap.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

import static cn.ldap.ldap.common.enums.ExceptionEnum.COLLECTION_EMPTY;


@Service
@Slf4j
public class OperationLogServiceImpl extends ServiceImpl<OperationMapper,OperationLogModel> implements OperationLogService {

    @Resource
    private OperationMapper operationMapper;

    @Override
    public ResultVo<List<OperationLogModel>> queryLog(LogDto logDto) {
        String beginTime = null;
        String endTime = null;
        if (ObjectUtils.isEmpty(logDto.getBeginTime())){
            beginTime = DateUtil.getBeginTime();
            endTime = DateUtil.getEndTime();
        }else {
            beginTime = logDto.getBeginTime();
            endTime = logDto.getEndTime();
        }
        QueryWrapper<OperationLogModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ge(OperationLogModel::getCreateTime,beginTime);
        queryWrapper.lambda().le(OperationLogModel::getCreateTime,endTime);
        queryWrapper.lambda().eq(!ObjectUtils.isEmpty(logDto.getOperateType()),OperationLogModel::getOperateType,logDto.getOperateType());
        IPage<OperationLogModel> page = new Page<>(logDto.getPageNum(), logDto.getPageSize());
        List<OperationLogModel> operationLogModels = operationMapper.selectPage(page, queryWrapper).getRecords();
        if(CollectionUtils.isEmpty(operationLogModels)){
            throw new SystemException(COLLECTION_EMPTY);
        }
        return ResultUtil.success(operationLogModels);
    }
}
