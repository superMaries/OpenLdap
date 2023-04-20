package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.AdminVo;
import cn.ldap.ldap.common.dto.UpdateAdminVo;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.UserAccountModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.UserEnableEnum;
import cn.ldap.ldap.common.enums.UserRoleEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.UserAccountMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.SessionUtil;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员管理
 *
 * @title: AdminUServiceImpl
 * @Author Wy
 * @Date: 2023/4/10 14:22
 * @Version 1.0
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<UserMapper, UserModel>
        implements AdminService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    /**
     * 查询管理管理列表
     *
     * @param adminVo 查询条件
     * @return 返回管理员管理数据
     */
    @Override
    public ResultVo<UserModel> queryAdminList(AdminVo adminVo) {
        List<UserModel> userModels = list(new LambdaQueryWrapper<UserModel>()
                .eq(!ObjectUtils.isEmpty(adminVo.getCertName()), UserModel::getCertName, adminVo.getCertName())
                .eq(!ObjectUtils.isEmpty(adminVo.getSertNo()), UserModel::getCertSn, adminVo.getSertNo()));
        return ResultUtil.success(userModels);
    }

    /**
     * 删除usbKey 用户
     *
     * @param adminVo
     * @return 返回tru 成功   false 失败
     */
    @Override
    public ResultVo<Boolean> delUserKey(AdminVo adminVo) {
        if (ObjectUtils.isEmpty(adminVo)
                || ObjectUtils.isEmpty(adminVo.getId())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        boolean b = removeById(adminVo.getId());
        return ResultUtil.success(b);
    }

    /**
     * 添加usbKey 用户
     *
     * @param userDto
     * @return 返回tru 成功   false 失败
     */
    @Override
    public ResultVo<Boolean> addUserKey(UserDto userDto) {
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
        // 实体化，参数设置
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

    /**
     * 修改密码
     *
     * @param adminVo 参数
     * @return 修改密码
     */
    @Override
    public ResultVo<Boolean> updatePwd(UpdateAdminVo adminVo, HttpServletRequest httpServletRequest) {
        if (ObjectUtils.isEmpty(adminVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }

        if (ObjectUtils.isEmpty(adminVo.getOldPassword())) {
            return ResultUtil.fail(ExceptionEnum.PASSWD);
        }
        if (ObjectUtils.isEmpty(adminVo.getPassword())) {
            return ResultUtil.fail(ExceptionEnum.PASSWD);
        }
        try {
           // LoginResultVo userInfo = SessionUtil.getUserInfo(httpServletRequest);
            List<UserAccountModel> userAccountModels = userAccountMapper.selectList(null);

            if (ObjectUtils.isEmpty(userAccountModels)) {
                return ResultUtil.success(ExceptionEnum.SUCCESS);
            }
            UserAccountModel userAccountModel = userAccountModels.size() > 0 ? userAccountModels.get(0) : new UserAccountModel();
            if (!adminVo.getOldPassword().equals(userAccountModel.getPassword())){
                throw new SysException(ExceptionEnum.OLD_PASSWORD_ERROR);
            }
            userAccountModel.setPassword(adminVo.getPassword());
            userAccountMapper.updateById(userAccountModel);
            return ResultUtil.success(true);
        } catch (Exception e) {
            return ResultUtil.fail(ExceptionEnum.SQL_SAVA_ERROR);
        }

    }

    /**
     * key是否初始化
     *
     * @param userDto
     * @return
     */
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
