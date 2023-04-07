package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ConfigEnum;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.UserEnableEnum;
import cn.ldap.ldap.common.enums.UserRoleEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public ResultVo isInit() {
        return ResultUtil.success(init());
    }

    public Map<String, Object> init() {
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
            if (ObjectUtils.isEmpty(users)) {
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
    public ResultVo importConfig(UserDto userDto) {
        if (ObjectUtils.isEmpty(userDto)
                || ObjectUtils.isEmpty(userDto.getServiceType())) {
            log.error("参数异常");
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        ConfigModel configModel = new ConfigModel();
        configModel.setIsInit(1);
        configModel.setServiceType(userDto.getServiceType());
        try {
            configService.save(configModel);
            return ResultUtil.success(true);
        } catch (Exception e) {
            //日志
            return ResultUtil.fail(ExceptionEnum.SQL_SAVA_ERROR);
        }
    }

    @Override
    public ResultVo importAdminKey(UserDto userDto) {
        log.info("导出管理员key接口");
        if (ObjectUtils.isEmpty(userDto)
                || ObjectUtils.isEmpty(userDto.getCertSn())
                || ObjectUtils.isEmpty(userDto.getSignCert())
                || ObjectUtils.isEmpty(userDto.getCertName())) {
            log.error("参数异常");
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }

        //判断该key 是否被初始化
        if (!adminKeyIsInit(userDto)) {
            log.info("该Key" + userDto.getCertName() + "已被初始化");
            return ResultUtil.fail(ExceptionEnum.USER_INIT, "该Key" + userDto.getCertName() + "已被初始化");
        }

        UserModel userModel = new UserModel();
        userModel.setCertSn(userDto.getCertSn());
        userModel.setSignCert(userDto.getSignCert());
        userModel.setCertName(userDto.getCertName());
        userModel.setIsEnable(UserEnableEnum.USER_ENALE.getCode());
        userModel.setRoleId(UserRoleEnum.USER_ADMIN.getCode());
        try {
            save(userModel);
            return ResultUtil.success(true);
        } catch (Exception e) {
            return ResultUtil.fail(ExceptionEnum.SQL_SAVA_ERROR);
        }
    }

    private boolean adminKeyIsInit(UserDto userDto) {
        LambdaQueryWrapper userWrapper = new LambdaQueryWrapper<UserModel>()
                .eq(UserModel::getCertSn, userDto.getCertSn())
                .eq(UserModel::getSignCert, userDto.getSignCert())
                .ne(UserModel::getIsEnable, UserEnableEnum.USER_DISABLE.getCode());
        List userList = this.list(userWrapper);
        if (ObjectUtils.isEmpty(userList)) {
            //未初始化
            return true;
        }
        return false;
    }
}
