package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.UserDto;
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
    public Map<String, Object> isInit() {
        return userService.isInit();
    }

    @PostMapping("importConfig")
    public boolean importConfig(@RequestBody UserDto userDto){
        return userService.importConfig(userDto);
    }
    @PostMapping("importAdminKey")
    public boolean importAdminKey(@RequestBody UserDto userDto) {
       return userService.importAdminKey(userDto);
    }






}
