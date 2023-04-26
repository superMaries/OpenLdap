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
import cn.ldap.ldap.common.util.*;
import cn.ldap.ldap.common.vo.LogVo;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.hander.InitConfigData;
import cn.ldap.ldap.service.OperationLogService;
import cn.ldap.ldap.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResultVo<Map<String, Object>> queryLog(LogDto logDto) {
        Map<String, Object> map = new HashMap<>();
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
        String operateType = logDto.getOperateType();

        List<LogVo> logVos = operationMapper.queryLog(pagePage, pageSize,
                beginTime, endTime, operateType);
        long count = operationMapper.countQueryLog(pagePage, pageSize,
                beginTime, endTime, operateType);
        //根据审计员ID获取用户信息
        List<Integer> aduitIds = logVos.stream().map(it -> it.getAuditId()).collect(Collectors.toList());
        List<UserModel> auditUser = userMapper.selectList(new LambdaQueryWrapper<UserModel>()
                .in(UserModel::getId, aduitIds));
        System.out.println("查询的数据为" + logVos);
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
        map.put("total", count);
        map.put("data", logVos);
        return ResultUtil.success(map);
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
        try {
            if (Sm2Util.verifyEx(InitConfigData.getPublicKey(), it.operateSrcToString(), it.getSignSrcEx())) {
                it.setAdminVerify(AdminVerifyEnum.SIGN_SUCCESS.getMsg());
            } else {
                it.setAdminVerify(AdminVerifyEnum.SIGN_ERROR.getMsg());
            }
        } catch (Exception e) {
            it.setAdminVerify(AdminVerifyEnum.SIGN_ERROR.getMsg());
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
            it.setAuditVerify(AdminVerifyEnum.NOT_SIGN.getMsg());
        } else if (Objects.equals(it.getAuditStatus(), StaticValue.AUDIT_STATUS)) {
            it.setAuditName(AuditEnum.AUDIT.getMsg());
            //验签审计的数据
            List<UserModel> collect = auditUser.stream().filter(audit -> Objects.equals(audit.getId(), it.getAuditId()))
                    .collect(Collectors.toList());
            UserModel userModel = ObjectUtils.isEmpty(collect) ? null : collect.get(0);
            System.out.println("查询审计原始数据为:  " + it.auditSrcToString());
            System.out.println("查询签名数据为:  " + it.getAuditSignValueEx());
            if (Sm2Util.verifyEx(InitConfigData.getPublicKey(), it.auditSrcToString(), it.getAuditSignValueEx())) {
                it.setAuditVerify(AdminVerifyEnum.SIGN_SUCCESS.getMsg());
            } else {
                it.setAuditVerify(AdminVerifyEnum.SIGN_ERROR.getMsg());
            }
            if (!ObjectUtils.isEmpty(userModel)) {
                //审计员名称
                it.setAuditName(userModel.getCertName());
            } else {
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
    public ResultVo<Map<String, Integer>> auditLog(HttpServletRequest request, List<AuditDto> auditDtos) {
        log.info("审计的参数：{}", auditDtos);
        if (ObjectUtils.isEmpty(auditDtos)) {
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        //根据id查询数据
        List<Integer> ids = auditDtos.stream().map(it -> it.getId()).collect(Collectors.toList());

        //查询到需要审计的数据
        List<OperationLogModel> operationLogs = list(new LambdaQueryWrapper<OperationLogModel>()
                .in(OperationLogModel::getId, ids));

        LoginResultVo userInfo = SessionUtil.getUserInfo(request);
        if (ObjectUtils.isEmpty(userInfo) || ObjectUtils.isEmpty(userInfo.getUserInfo())) {
            log.info("用户未登录或已过期");
            throw new SysException(ExceptionEnum.USER_NOT_LOGIN);
        }
        Integer errorCount = StaticValue.ERROR_COUNT;
        Integer successCount = StaticValue.SUCCESS_COUNT;

        //对每一条的日志进行批量处理
        for (OperationLogModel it : operationLogs) {
            //获取到需要更新的数据
            AuditDto auditDto = auditDtos.stream()
                    .filter(audit -> it.getId().equals(audit.getId()))
                    .collect(Collectors.toList()).get(0);
            if (ObjectUtils.isEmpty(auditDto)) {
                log.error("未找到数据");
                errorCount++;
                continue;
            }

            if (!checkAdminVerity(userInfo, auditDto)) {
                errorCount++;
                continue;
            }
            try {
                //设置前端传递的签名值
                it.setAuditSignValue(ObjectUtils.isEmpty(auditDto.getAuditSignValue()) ? "" : auditDto.getAuditSignValue());
                String strSignValue = Sm2Util.sign(InitConfigData.getPrivateKey(),
                        auditDto.toAuditSrc());
                it.setAuditSignValueEx(strSignValue);
                System.out.println("审计原数据：" + auditDto.toAuditSrc());
                System.out.println("审计签名值：" + strSignValue);
            } catch (Exception e) {
                log.error(e.getMessage());
                errorCount++;
                continue;
            }
            it.setAuditId(auditDto.getAuditId());
            it.setAuditTime(auditDto.getAuditTime());
            System.out.println("审计时间：" + it.getAuditTime());
            it.setAuditStatus(AuditEnum.AUDIT.getCode());
            it.setPass(auditDto.getAuditStatus());
            it.setRemark(auditDto.getRemark());
            try {
                boolean b = saveOrUpdate(it);
                if (b) {
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("保存日志错误:{}", e.getMessage());
                errorCount++;
            }
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("success", successCount);
        map.put("error", errorCount);
        return ResultUtil.success(map);
    }

    private boolean checkAdminVerity(LoginResultVo userInfo, AuditDto auditDto) {
        //ADMIN用户不要验签  KEY需要验签
        if (!UserRoleEnum.ACCOUNT_ADMIN.getCode().equals(userInfo.getUserInfo().getRoleId())) {
            //usbKey的用户进行验签
            //获取当前用户的证书
            String certData = userInfo.getUserInfo().getCertData();
            log.info("用户证书:{}", certData);
            //对数据进行验签
            String src = auditDto.toSrc();
            try {
                boolean verify = Sm2Util.verify(certData, src, auditDto.getAuditSignValue());
                if (verify) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public ResultVo<List<String>> queryOperateType() {
        List<OperationLogModel> operationLogModels = list();
        List<String> distinct = operationLogModels.stream().map(it -> it.getOperateObject()).distinct().collect(Collectors.toList());
        return ResultUtil.success(distinct);
    }
}
