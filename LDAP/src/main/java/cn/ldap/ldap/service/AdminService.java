package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.AdminVo;
import cn.ldap.ldap.common.dto.UpdateAdminVo;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.vo.ResultVo;

/**
 * 管理员管理
 * @title: AdminUService
 * @Author Wy
 * @Date: 2023/4/10 14:09
 * @Version 1.0
 */
public interface AdminService {
    /**
     * 查询管理管理列表
     * @param adminVo  查询条件
     * @return 返回管理员管理数据
     */
    ResultVo<UserModel> queryAdminList(AdminVo adminVo);
    /**
     * 删除usbKey 用户
     * @param adminVo
     * @return 返回tru 成功   false 失败
     */
    ResultVo<Boolean> delUserKey(AdminVo adminVo);

    /**
     * 添加usbKey 用户
     * @param userDto
     * @return 返回tru 成功   false 失败
     */
    ResultVo<Boolean> addUserKey(UserDto userDto);

    /**
     * 修改密码
     * @param adminVo 参数
     * @return 返回tru 成功   false 失败
     */
    ResultVo<Boolean> updatePwd(UpdateAdminVo adminVo);
}
