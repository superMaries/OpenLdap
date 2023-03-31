package cn.ldap.ldap.common.aop;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.enums.LogStateTypeEnum;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.mapper.OperationMapper;
import cn.ldap.ldap.common.util.ClientInfo;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @title: OperateLogAspect
 * @Author Wy
 * @Date: 2023/3/31 15:37
 * @Version 1.0
 */
@Component
@Aspect
@Slf4j
public class OperateLogAspect {

    @Autowired
    OperationMapper operationMapper;
    /**
     * 本地共享变量
     */
    static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    /**
     * 定义切入点
     *
     * @param operateAnnotation operateAnnotation 切注解@operateAnnotation
     */
    @Pointcut(value = "@annotation(operateAnnotation)", argNames = "operateAnnotation")
    public void pointCut(cn.ldap.ldap.common.aop.annotations.OperateAnnotation operateAnnotation) {
    }

    @Before(value = "@annotation(cn.ldap.ldap.common.aop.annotations.OperateAnnotation)")
    public void setParam(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();
        // 获取操作
        Object param = null;

        OperateAnnotation operateAnnotation = method.getAnnotation(OperateAnnotation.class);

        if (!ObjectUtils.isEmpty(joinPoint.getArgs())) {
            param = joinPoint.getArgs()[0];
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //获取登录的信息

        // 构建日志实体
        OperationLogModel operationLogModel = new OperationLogModel();
        StringBuffer remark = new StringBuffer();
        //操作中文名称
        String name = operateAnnotation.operateType().getName();
        //记录日志时  登录登出 初始化需要特殊处理 并且不喜欢需要操作员验签
        boolean isLogin = false;
        String certData = "";
        String signOriginalData = "";
        //获取当前用户 如果是登录 注册 初始化
        if (operateAnnotation.operateModel().getCode().equals(OperateMenuEnum.USER_MANAGER.getCode())
                && !operateAnnotation.operateType().equals(OperateTypeEnum.USER_LOGOUT)) {

            if (operateAnnotation.operateType().equals(OperateTypeEnum.USER_LOGIN)) {
                UserDto userDto = (UserDto) param;
                if (OperateTypeEnum.USER_LOGIN.equals(operateAnnotation.operateType())) {
                    isLogin = true;
                    remark.append(OperateTypeEnum.USER_INIT.getName());
                } else {
                    //获取用户信息Id 登录的操作
                    operationLogModel.setUserId(1);
                    remark.append("用户：").append("").append(name);
                }
            }
            if (operateAnnotation.operateType().equals(OperateTypeEnum.USER_REGIS)) {
                isLogin = true;
                remark.append(OperateTypeEnum.USER_REGIS.getName());
            }
            if (operateAnnotation.operateType().equals(OperateTypeEnum.USER_INIT)) {
                isLogin = true;
                remark.append(OperateTypeEnum.USER_INIT.getName());
            }
        } else {
            operationLogModel.setUserId(1);
            remark.append("用户：").append("").append(name);
        }
        String clientIP = ClientInfo.getIpAdrress(request);
        operationLogModel.setClientIp(clientIP);
        operationLogModel.setOperateType(operateAnnotation.operateType().getName());
        operationLogModel.setOperateMenu(operateAnnotation.operateModel().getName());
        operationLogModel.setRemark(operateAnnotation.remark());
        operationLogModel.setRemark(remark.toString());

        log.info(!ObjectUtils.isEmpty(operateAnnotation.remark()) ?
                operateAnnotation.remark() : name);
        switch (operateAnnotation.operateModel()) {
            case USER_MANAGER:
                switch (operateAnnotation.operateType()) {
                    case USER_LOGIN:
                        threadLocal.set(operationLogModel);
                        break;
                    case USER_REGIS:
                        threadLocal.set(operationLogModel);
                        break;
                    case USER_LOGOUT:
                        threadLocal.set(operationLogModel);
                        break;
                    case USER_INIT:
                        threadLocal.set(operationLogModel);
                        break;
                    case DOWN_CLIENT:
                        threadLocal.set(operationLogModel);
                        break;
                    case LOOK_MANUAl:
                        threadLocal.set(operationLogModel);
                        break;
                    case OPERATE_QUERY:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
                        log.warn("未知类型操作：{}", operateAnnotation.operateType().getName());
                }
                break;
            case INDEX_MANAGER:
                switch (operateAnnotation.operateType()) {
                    //暂无
                }
                break;
            case CATALOGUE_MANAGER:
                switch (operateAnnotation.operateType()) {
                    case LOOK_DATA:
                        threadLocal.set(operationLogModel);
                        break;
                }
                break;

            case PARAM_MANAGER:
                switch (operateAnnotation.operateType()) {
                    case LOOK_PARAM:
                        threadLocal.set(operationLogModel);
                        break;
                    case UPDATE_PARAM:
                        threadLocal.set(operationLogModel);
                        break;
                }
                threadLocal.set(operationLogModel);
                break;
            case LOG_MANAGER:
                switch (operateAnnotation.operateType()) {
                    //暂无
                }
                break;
            default:
                log.info("无法识别操作");
//                remark.append("用户：").append(userInfo.getCertName()).append(",无法识别当前操作");
                operationLogModel.setRemark(remark.toString());
                threadLocal.set(operationLogModel);
                break;
        }
    }


    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     */
    @AfterReturning(value = "@annotation(cn.ldap.ldap.common.aop.annotations.OperateAnnotation)",
            returning = "returnValue")
    public void saveOperateAnnotation(JoinPoint joinPoint, Object returnValue) {
        // 其他操作
        Object o = threadLocal.get();
        // 没有返回值不记录日志  当为下载的时候,这里为空.
        if (ObjectUtils.isEmpty(returnValue)) {
            return;
        }
        if (ObjectUtils.isEmpty(o)) {
            return;
        }
        OperationLogModel operationLog = (OperationLogModel) o;
        if (returnValue instanceof Boolean) {
            if ((Boolean) returnValue) {
                operationLog.setOperateState(LogStateTypeEnum.SUCCEED.getCode());
            } else {
                operationLog.setOperateState(LogStateTypeEnum.FAIL.getCode());
            }
        } else {
            operationLog.setOperateState(LogStateTypeEnum.SUCCEED.getCode());
        }
        operationMapper.insert(operationLog);

    }
    /**
     * 异常后记录日志
     *
     * @param joinPoint joinPoint
     */
    @AfterThrowing(value = "@annotation(cn.ldap.ldap.common.aop.annotations.OperateAnnotation)",
            throwing = "ex")
    public void saveOperateAnnotationAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        Object object = threadLocal.get();
        if (ObjectUtils.isEmpty(object)) {
            return;
        }
        //获取当前用户
//        UserRedisInfo userInfo = userInfoUtils.getUserInfo();
//        if (ObjectUtils.isEmpty(userInfo)) {
//            throw new SysException(ResultEnum.USER_SESSION_TIMEOUT);
//        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();
        OperateAnnotation operateAnnotation = method.getAnnotation(OperateAnnotation.class);
        OperationLogModel operationLog = (OperationLogModel) object;
        operationLog.setOperateState(LogStateTypeEnum.FAIL.getCode());

        /**
         * 最大截取200长度.
         */
        String remark = "";
        if (!ObjectUtils.isEmpty(ex.getMessage()) && ex.getMessage().length() > 200) {
            if (!ObjectUtils.isEmpty(operateAnnotation.remark())) {
                remark = operateAnnotation.remark() + ex.getMessage().substring(0, 199);
            } else {
                remark = ex.getMessage().substring(0, 199);
            }
        } else {
            if (!ObjectUtils.isEmpty(operateAnnotation.remark())) {
                remark = operateAnnotation.remark() + ex.getMessage();
            } else {
                remark = ex.getMessage();
            }
        }
        operationLog.setRemark(remark);
//        operationLog.setUserId(userInfo.getId());
        operationLog.setOperateType(operateAnnotation.operateType().getName());
        operationLog.setOperateMenu(operateAnnotation.operateModel().getName());
        operationMapper.insert(operationLog);
        threadLocal.remove();
    }
}
