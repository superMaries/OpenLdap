package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.vo.ObjectDataDto;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.ObjectClassInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
/**
 *
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/objectClass/")
public class ObjectClassInformationController {

    @Resource
    private ObjectClassInformationService objectClassInformationService;

    /**
     * 查询所有objectClass和属性
     * @return
     */
    @PostMapping("queryInformation")
    public ResultVo<List<ObjectDataDto>> queryInformation() {
        return objectClassInformationService.queryObjectAndAttribute();
    }
}
