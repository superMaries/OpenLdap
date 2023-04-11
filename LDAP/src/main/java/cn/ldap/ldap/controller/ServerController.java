package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/server/")
public class ServerController {

    @Resource
    private IndexRuleService indexRuleService;

    @PostMapping("sslOperation")
    @OperateAnnotation(operateModel = OperateMenuEnum.PARAM_MANAGER, operateType = OperateTypeEnum.UPLOAD_FILE)
    public ResultVo<Object> sslOperation(@RequestBody ServerDto serverDto) {
        return indexRuleService.sslOperation(serverDto);
    }
}
