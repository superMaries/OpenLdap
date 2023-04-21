package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.AddLogDto;
import cn.ldap.ldap.common.dto.AuditDto;
import cn.ldap.ldap.common.dto.LogDto;
import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.LogVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import cn.ldap.ldap.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 配置接口
 *
 * @title: CertController
 * @Author superMarie
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/config/")
public class ConfigController {

    @Resource
    private LdapConfigService ldapConfigService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 添加配置
     *
     * @param mainConfig
     * @return
     */
    @PostMapping("addConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPDATE_PARAM)
    public ResultVo<T> addConfig(@RequestBody MainConfig mainConfig) throws IOException {
        return ldapConfigService.addConfig(mainConfig);
    }

    /**
     * 开启或关闭服务
     *
     * @param openOrClose
     * @return
     * @throws IOException
     */
    @PostMapping("setServerStatus/{openOrClose}")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.OPEN_SERVICE)
    public ResultVo<String> setServerStatus(@PathVariable("openOrClose") Boolean openOrClose) throws IOException {
        return ldapConfigService.setServerStatus(openOrClose);
    }

    /**
     * 判断服务是否启动
     *
     * @return
     */
    @GetMapping("getServerStatus")
//    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.LOOK_PARAM)
    public Boolean getServerStatus() {
        return ldapConfigService.getServerStatus();
    }

    /**
     * 日志查询
     *
     * @param logDto
     * @return
     */
    @PostMapping("queryLog")
//    @OperateAnnotation(operateModel = OperateMenuEnum.LOG_MANAGER, operateType = OperateTypeEnum.OPERATE_QUERY)
    public ResultVo<List<LogVo>> queryLog(@RequestBody LogDto logDto) {
        return operationLogService.queryLog(logDto);
    }

    /**
     * 添加日志
     *
     * @return true成功  false 失败
     */
    @PostMapping("addLog")
    public ResultVo<Boolean> addLog(HttpServletRequest request, @RequestBody AddLogDto logDto) {
        return operationLogService.addLog(request, logDto);
    }

    /**
     * 审计日志
     *
     * @return true成功  false 失败
     */
    @PostMapping("auditLog")
    public ResultVo<Boolean> auditLog(HttpServletRequest request, @RequestBody List<AuditDto> auditDtos) {
        return operationLogService.auditLog(request, auditDtos);
    }
}
