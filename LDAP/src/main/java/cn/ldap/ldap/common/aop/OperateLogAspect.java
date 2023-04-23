package cn.ldap.ldap.common.aop;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.*;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.OperationMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ClientInfo;
import cn.ldap.ldap.common.util.SessionUtil;
import cn.ldap.ldap.common.util.Sm2Util;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.hander.InitConfigData;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cn.ldap.ldap.common.enums.OperateTypeEnum.ADD_USBKEY;
import static cn.ldap.ldap.common.enums.OperateTypeEnum.DEL_USBKEY;

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
    private OperationMapper operationMapper;
    @Autowired
    private UserMapper userMapper;
    private final static String SIGN = "sign";
    private final static String ORIGN = "orgin";
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

        //获取头部的签名值
        String sign = request.getHeader(SIGN);
        // 原数据
        String orgin = request.getHeader(ORIGN);
        if (ObjectUtils.isEmpty(sign) || ObjectUtils.isEmpty(orgin)) {
            log.error("未传递签名值和原数据");
        } else {
            try {
                orgin = URLDecoder.decode(orgin, "UTF-8");
                sign = URLDecoder.decode(sign, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.HEADER_ERROR);
            }
        }
        // 构建日志实体
        OperationLogModel operationLogModel = new OperationLogModel();
        StringBuffer remark = new StringBuffer();
        //操作中文名称
        String name = operateAnnotation.operateType().getName();
        //记录日志时  登录登出 初始化需要特殊处理 并且不需要操作员验签
        boolean isLogin = false;
        //获取登录的信息
        LoginResultVo userInfo = null;
        try {
            userInfo = SessionUtil.getUserInfo(request);
        } catch (SysException e) {
            userInfo = null;
        }
        /**
         * 如果是用户管理下面的话就不需要记录谁做的
         */
        if (OperateMenuEnum.USER_MANAGER.equals(operateAnnotation.operateModel())) {
            //除了登出 其余不需要记录哪个用户操作
            if (OperateTypeEnum.USER_LOGOUT.equals(operateAnnotation.operateType())) {
                if (ObjectUtils.isEmpty(userInfo)) {
                    return;
                }
                if (UserRoleEnum.ACCOUNT_ADMIN.getCode().equals(userInfo.getUserInfo().getRoleId())) {
                    //说明是admin 登录的退出
                    operationLogModel.setUserId(0);
//                    remark.append("用户：").append(UserRoleEnum.ACCOUNT_ADMIN.getMsg()).append(name);
                } else {
                    operationLogModel.setUserId(userInfo.getUserInfo().getId());
//                    remark.append("用户：").append(userInfo.getUserInfo().getRoleName()).append(name);
                }
            } else {
                //session 中 没有值
                isLogin = true;
                //判断是amdin 还是 usebKey 登录， usebKey 登录就需要对数据进行验签
                if (param instanceof UserDto) {
                    // usebKey登录
                    try {
                        String signCert = ((UserDto) param).getSignCert();
                        boolean verify = Sm2Util.verify(signCert, orgin, sign);
                        if (!verify) {
                            log.error("{}", ExceptionEnum.SIGN_DATA_ERROR.getMessage());
                            throw new SysException(ExceptionEnum.SIGN_DATA_ERROR);
                        }
                    } catch (Exception e) {
                        log.error("{}:{}", ExceptionEnum.SIGN_DATA_ERROR.getMessage(), e.getMessage());
                        throw new SysException(ExceptionEnum.SIGN_DATA_ERROR);
                    }
                }
//                remark.append(name);
            }
        } else {
            if (ObjectUtils.isEmpty(userInfo)) {
                log.info("登录已过期");
                throw new SysException(ExceptionEnum.USER_NOT_LOGIN);
            }
            if (!(UserRoleEnum.ACCOUNT_ADMIN.getCode().equals(userInfo.getUserInfo().getId()))) {
                //ADMIN 用户不做验签
                UserModel userModel = userMapper.selectById(userInfo.getUserInfo().getId());
                String signCert = userModel.getSignCert();
                try {
                    boolean verify = false;
                    verify = Sm2Util.verify(signCert, orgin, sign);
                    if (!verify) {
                        log.error("{}", ExceptionEnum.SIGN_DATA_ERROR.getMessage());
                        throw new SysException(ExceptionEnum.SIGN_DATA_ERROR);
                    }
                } catch (Exception e) {
                    throw new SysException(ExceptionEnum.SIGN_DATA_ERROR);
                }
            }
        }
        String clientIp = ClientInfo.getIpAdrress(request);
        operationLogModel.setClientIp(clientIp);
        operationLogModel.setOperateType(operateAnnotation.operateType().getName());
        operationLogModel.setOperateMenu(operateAnnotation.operateModel().getName());
        operationLogModel.setOperateObject(operateAnnotation.operateModel()
                + StaticValue.LINE + operateAnnotation.operateType());
//        operationLogModel.setRemark(remark.toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        operationLogModel.setCreateTime(simpleDateFormat.format(new Date()));
        //设置签名值
        operationLogModel.setSignValue(sign);
        //设置签名原数据
        operationLogModel.setSignSrc(orgin);

//        log.info(!ObjectUtils.isEmpty(operateAnnotation.remark()) ?
//                operateAnnotation.remark() : name);
        settTreadLocal(operateAnnotation, operationLogModel, remark, threadLocal);
    }

    private void settTreadLocal(OperateAnnotation operateAnnotation, OperationLogModel operationLogModel, StringBuffer remark, ThreadLocal<Object> threadLocal) {
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
                    case IMPORT_ADMIN_KEY:
                        threadLocal.set(operationLogModel);
                        break;
                    case QUERY_VERSION:
                        threadLocal.set(operationLogModel);
                        break;
                    case QUERY_MENUS:
                        threadLocal.set(operationLogModel);
                        break;
                    case USER_IS_INIT:
                        threadLocal.set(operationLogModel);
                        break;
                    case SERVICE_MODEL:
                        threadLocal.set(operationLogModel);
                        break;
                    case IMPORT_CONFIG:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
                        log.warn("未知类型操作：{}", operateAnnotation.operateType().getName());
                }
                break;
            case INDEX_MANAGER:
                switch (operateAnnotation.operateType()) {
                    //暂无
                    default:
                        break;
                }
                break;
            case CATALOGUE_MANAGER:
                switch (operateAnnotation.operateType()) {
                    case LOOK_DATA:
                        threadLocal.set(operationLogModel);
                        break;
                    case DEL_LDAP:
                        threadLocal.set(operationLogModel);
                        break;
                    case EDIT_LDAP_ATTRITE:
                        threadLocal.set(operationLogModel);
                        break;
                    case MIDIFY_LDAP_NAME:
                        threadLocal.set(operationLogModel);
                        break;
                    case EXPORT_ALL:
                        threadLocal.set(operationLogModel);
                        break;
                    case EXPORT_LDIF:
                        threadLocal.set(operationLogModel);
                        break;
                    case IMPORT_LDAP:
                        threadLocal.set(operationLogModel);
                        break;
                    case ADD_LDAF:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
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
                    case UPLOAD_FILE:
                        threadLocal.set(operationLogModel);
                        break;
                    case OPEN_SERVICE:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
                        break;
                }
                break;
            case LOG_MANAGER:
                switch (operateAnnotation.operateType()) {
                    //暂无
                    default:
                        break;
                }
                break;
            case ADMIN_MANAGER:
                switch (operateAnnotation.operateType()) {
                    case DEL_USBKEY:
                        threadLocal.set(operationLogModel);
                        break;
                    case ADD_USBKEY:
                        threadLocal.set(operationLogModel);
                        break;
                    case UPDATE_USBKEY:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
                        break;
                }
                break;
            case INDEX:
                switch (operateAnnotation.operateType()) {
                    case INDEX_UPDATE_OR_INTER:
                        threadLocal.set(operationLogModel);
                        break;
                    case INDEX_DELETE:
                        threadLocal.set(operationLogModel);
                        break;
                    default:
                        break;
                }
                break;
            default:
                log.info("无法识别操作");
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
            Integer code = ((ResultVo) returnValue).getCode();
            operationLog.setOperateState(code);
        }
        //对原始数据+返回编码进行再次签名
        String newSrc = operationLog.getSignValue() + operationLog.getOperateState();
        operationLog.setSignSrc(newSrc);

        String sign = Sm2Util.sign(InitConfigData.getPrivateKey(), newSrc);
        operationLog.setSignValue(sign);

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
        if (!ObjectUtils.isEmpty(ex.getMessage()) && ex.getMessage().length() > StaticValue.MSG_LENGTH) {
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
