package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户
 *
 * @title: UserController
 * @Author Wy
 * @Date: 2023/3/31 9:12
 * @Version 1.0
 */
@RestController
@RequestMapping("/user/")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 是否初始化
     *
     * @return
     */
    @PostMapping("isInit")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.USER_IS_INIT)
    public ResultVo<Map<String, Object> > isInit() {
        return userService.isInit();
    }

    /**
     * 导入服务配置接口
     * @param userDto
     * @return
     */
//    @PostMapping("importConfig")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.IMPORT_CONFIG)
    public ResultVo<Boolean> importConfig(@RequestBody UserDto userDto){
        return userService.importConfig(userDto);
    }

    /**
     * 导出管理员key接口
     * @param userDto
     * @return
     */
    @PostMapping("importAdminKey")
    @OperateAnnotation(operateModel = OperateMenuEnum.USER_MANAGER, operateType = OperateTypeEnum.IMPORT_ADMIN_KEY)
    public ResultVo<Boolean> importAdminKey(@RequestBody UserDto userDto) {
       return userService.importAdminKey(userDto);
    }






}
