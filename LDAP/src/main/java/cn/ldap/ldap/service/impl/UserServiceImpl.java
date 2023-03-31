package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ConfigEnum;
import cn.ldap.ldap.common.enums.UserEnableEnum;
import cn.ldap.ldap.common.enums.UserRoleEnum;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserModel>
        implements UserService {

    @Autowired
    private ConfigServiceImpl configService;


    /**
     * 是否初始化
     *
     * @return
     */
    @Override
    public Map<String, Object> isInit() {
        log.info("是否初始化");
        Map<String, Object> mapVo = new HashMap<>();
        mapVo.put("isInit", false);
        List<ConfigModel> configLists = configService.list();
        //系统未初始化,在第一步
        if (ObjectUtils.isEmpty(configLists)) {
            log.info("系统未初始化,在第一步");
            mapVo.put("isInit", true);
            mapVo.put("step", 0);
        } else {
            List<UserModel> users = list(null);
            //系统未初始化,在第二步
            if (ObjectUtils.isEmpty(users) || users.size() == 0) {
                log.info("系统未初始化,在第二步");
                mapVo.put("isInit", true);
                mapVo.put("step", 1);
                return mapVo;
            } else if (users.size() == 1) {
                //系统未初始化,在第三步
                log.info("系统未初始化,在第三步");
                mapVo.put("isInit", true);
                mapVo.put("step", 2);
                return mapVo;
            }
        }
        return mapVo;
    }

    @Override
    public boolean importConfig(UserDto userDto) {
        if (ObjectUtils.isEmpty(userDto)
                || ObjectUtils.isEmpty(userDto.getServiceType())) {
            log.error("参数异常");
            return false;
        }
        ConfigModel configModel = new ConfigModel();
        configModel.setIsInit(1);
        configModel.setServiceType(userDto.getServiceType());
        try {
            configService.save(configModel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean importAdminKey(UserDto userDto) {
        log.info("导出管理员key接口");
        if (ObjectUtils.isEmpty(userDto)
                || ObjectUtils.isEmpty(userDto.getCertSn())
                || ObjectUtils.isEmpty(userDto.getSignCert())) {
            log.error("参数异常");
            return false;
        }

        UserModel userModel = new UserModel();
        userModel.setCertSn(userDto.getCertSn());
        userModel.setSignCert(userDto.getSignCert());
        userModel.setIsEnable(UserEnableEnum.USER_ENALE.getCode());
        userModel.setRoleId(UserRoleEnum.USER_ADMIN.getCode());
        try {
            save(userModel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
