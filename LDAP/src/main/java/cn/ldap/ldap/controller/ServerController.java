package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
import cn.ldap.ldap.service.PortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/server/")
public class ServerController {

    @Resource
    private IndexRuleService indexRuleService;

    @Resource
    private PortLinkService portLinkService;

    @PostMapping("sslOperation")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<Object> sslOperation(@RequestBody ServerDto serverDto) {
        return indexRuleService.sslOperation(serverDto);
    }
    @GetMapping("queryServerStatus")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<List<PortLink>> queryServerStatus(){
        return ResultUtil.success(portLinkService.list());
    }
}
