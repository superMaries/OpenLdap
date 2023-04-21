package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.AddLogDto;
import cn.ldap.ldap.common.dto.AuditDto;
import cn.ldap.ldap.common.dto.LogDto;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.*;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.OperationMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ClientInfo;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.Sm2Util;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.LogVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.hander.InitConfigData;
import cn.ldap.ldap.service.OperationLogService;
import cn.ldap.ldap.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.SystemException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class OperationLogServiceImpl extends ServiceImpl<OperationMapper, OperationLogModel> implements OperationLogService {

    @Resource
    private OperationMapper operationMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * @title:
     * @Author superMarie
     * @Version 1.0
     */
    @Override
    public ResultVo<List<LogVo>> queryLog(LogDto logDto) {
        String beginTime = null;
        String endTime = null;
        //设置时间
        if (ObjectUtils.isEmpty(logDto.getBeginTime())) {
            beginTime = DateUtil.getBeginTime();
            endTime = DateUtil.getEndTime();
        } else {
            beginTime = logDto.getBeginTime();
            endTime = logDto.getEndTime();
        }
        //设置参数
        long pageSize = logDto.getPageSize();
        long pagePage = (logDto.getPageNum() - 1) * logDto.getPageSize();

        List<LogVo> logVos = operationMapper.queryLog(pagePage, pageSize, beginTime, endTime);
        //根据审计员ID获取用户信息
        List<Integer> aduitIds = logVos.stream().map(it -> it.getAuditId()).collect(Collectors.toList());
        List<UserModel> auditUser = userMapper.selectList(new LambdaQueryWrapper<UserModel>()
                .in(UserModel::getId, aduitIds));

        logVos.forEach(it -> {
            //设置操作状态
            setOperateStateName(it);
            getLogs(it);
            try {
                setAuditData(it, auditUser);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return ResultUtil.success(logVos);
    }

    /**
     * 设置操作状态
     *
     * @param it 日志VO
     */
    private void setOperateStateName(LogVo it) {
        it.setOperateObject(it.getOperateMenu() + StaticValue.LINE + it.getOperateType());
        if (Objects.equals(it.getOperateState(), LogStateTypeEnum.SUCCEED.getCode())) {
            it.setOperateStateName(LogStateTypeEnum.SUCCEED.getName());
        } else {
            it.setOperateStateName(LogStateTypeEnum.FAIL.getName());
        }
    }

    /**
     * 获取验签结果
     *
     * @param it 日志Vo
     */
    private void getLogs(LogVo it) {

        //设置操作验签结果 判断是Admin登录还是其他操作员操作
        if (ObjectUtils.isEmpty(it.getUserId()) || Objects.equals(it.getUserId(), StaticValue.ADMIN_ID)) {
            it.setAdminVerify(AdminVerifyEnum.NOT_SIGN.getMsg());
        } else {
//            if (Sm2Util.verify(it.getSignCert(), it.operateSrcToString(), it.getSignVlue())) {
            try {
                if (Sm2Util.verifyEx(InitConfigData.getPublicKey(), it.getSignSrc(), it.getSignVlue())) {
                    it.setAdminVerify(AdminVerifyEnum.SIGN_SUCCESS.getMsg());
                } else {
                    it.setAdminVerify(AdminVerifyEnum.SIGN_ERROR.getMsg());
                }
            } catch (Exception e) {
                it.setAdminVerify(AdminVerifyEnum.SIGN_ERROR.getMsg());
            }
        }

    }

    /**
     * 设置审计状态  、审计员名称、审计验签结果
     *
     * @param it        日志Vo
     * @param auditUser 审计员信息
     */
    private void setAuditData(LogVo it, List<UserModel> auditUser) throws IOException {
        //设置审计状态  、审计员名称、审计验签结果
        if (ObjectUtils.isEmpty(it.getAuditId()) || Objects.equals(it.getAuditStatus(), StaticValue.AUDIT_NOT_STATUS)) {
            //未审计
            it.setAuditStatusName(AuditEnum.NOT_AUDIT.getMsg());
        } else if (Objects.equals(it.getAuditStatus(), StaticValue.AUDIT_STATUS)) {
            it.setAuditName(AuditEnum.AUDIT.getMsg());
            //验签审计的数据
            List<UserModel> collect = auditUser.stream().filter(audit -> Objects.equals(audit.getId(), it.getAuditId()))
                    .collect(Collectors.toList());
            UserModel userModel = ObjectUtils.isEmpty(collect) ? null : collect.get(0);
            if (!ObjectUtils.isEmpty(userModel)) {
                //审计员名称
                it.setAuditName(userModel.getCertName());
                String signCert = userModel.getSignCert();
                if (Sm2Util.verify(signCert, it.auditSrcToString(), it.getSignVlue())) {
                    it.setAuditVerify(AdminVerifyEnum.SIGN_SUCCESS.getMsg());
                } else {
                    it.setAuditVerify(AdminVerifyEnum.NOT_SIGN.getMsg());
                }
            } else {
                it.setAuditVerify(AdminVerifyEnum.NOT_SIGN.getMsg());
                it.setAuditName(UserRoleEnum.ACCOUNT_ADMIN.getMsg());
            }
        } else {
            //未知数据
            it.setAuditStatusName(AuditEnum.UNKNOWN.getMsg());
        }
    }

    @Override
    public ResultVo<Boolean> addLog(HttpServletRequest request, AddLogDto logDto) {
        if (ObjectUtils.isEmpty(logDto)) {
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        //判断用户是否是审计员
        UserModel userModel = userMapper.selectById(logDto.getUserId());
        if (!ObjectUtils.isEmpty(userModel)) {
            if (Objects.equals(userModel.getRoleId(), UserRoleEnum.USER_ADMIN.getCode())) {
                log.info(ExceptionEnum.NOT_AUDIT.getMessage());
                throw new SysException(ExceptionEnum.NOT_AUDIT);
            }
        }
        String clinetIp = ClientInfo.getIpAdrress(request);
        OperationLogModel operationLog = new OperationLogModel();
        operationLog.setClientIp(clinetIp);
        //获取当前登录的用户
//        LoginResultVo userInfo = SessionUtil.getUserInfo(request);
        operationLog.setUserId(logDto.getUserId());
        operationLog.setOperateMenu(logDto.getOperateMenu());
        operationLog.setOperateType(logDto.getOperateType());
        operationLog.setOperateObject(logDto.getOperateObject());
        operationLog.setOperateState(logDto.getOperateStatus());
        operationLog.setFailCode(logDto.getFailCode());
        operationLog.setCreateTime(logDto.getCreateTime());
        operationLog.setSignSrc(logDto.getSignSrc());
        operationLog.setSignValue(logDto.getSignValue());
        try {
            boolean b = saveOrUpdate(operationLog);
            return ResultUtil.success(b);
        } catch (Exception e) {
            log.error("保存日志错误:{}", e.getMessage());
            return ResultUtil.success(false);
        }
    }

    /**
     * 审计日志
     *
     * @return true成功  false 失败
     */
    @Override
    public ResultVo<Boolean> auditLog(HttpServletRequest request, List<AuditDto> auditDtos) {
        if (ObjectUtils.isEmpty(auditDtos)) {
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        //根据id查询数据
        List<Integer> ids = auditDtos.stream().map(it -> it.getId()).collect(Collectors.toList());

        List<OperationLogModel> operationLogs = list(new LambdaQueryWrapper<OperationLogModel>()
                .in(OperationLogModel::getId, ids));


        operationLogs.forEach(it -> {
            AuditDto auditDto = auditDtos.stream()
                    .filter(audit -> it.getId().equals(audit.getId()))
                    .collect(Collectors.toList()).get(0);
            it.setAuditId(auditDto.getAuditId());
            it.setAuditTime(auditDto.getAuditTime());
            it.setAuditStatus(AuditEnum.AUDIT.getCode());
            it.setPass(auditDto.getAuditStatus());
            it.setAuditSrc(auditDto.getAuditSrc());
            it.setAuditSignValue(auditDto.getAuditSignValue());
            it.setRemark(auditDto.getRemark());
        });
        try {
            boolean b = saveOrUpdateBatch(operationLogs);
            return ResultUtil.success(b);
        } catch (Exception e) {
            log.error("保存日志错误:{}", e.getMessage());
            return ResultUtil.success(false);
        }
    }
}
