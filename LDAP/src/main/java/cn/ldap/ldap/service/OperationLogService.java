package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.AddLogDto;
import cn.ldap.ldap.common.dto.AuditDto;
import cn.ldap.ldap.common.dto.LogDto;
import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.vo.LogVo;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface OperationLogService extends IService<OperationLogModel> {

    ResultVo<List<LogVo>> queryLog(LogDto logDto);

    /**
     * 添加日志
     * @return  true成功  false 失败
     */
    ResultVo<Boolean> addLog(HttpServletRequest request,AddLogDto logDto);
    /**
     * 审计日志
     *
     * @return true成功  false 失败
     */
    ResultVo<Boolean> auditLog(HttpServletRequest request, List<AuditDto> auditDtos);

}
