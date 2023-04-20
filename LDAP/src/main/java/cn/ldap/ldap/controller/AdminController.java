package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.AdminVo;
import cn.ldap.ldap.common.dto.UpdateAdminVo;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员管理接口
 *
 * @title: AdminController
 * @Author Wy
 * @Date: 2023/4/10 13:55
 * @Version 1.0
 */
@RestController
@RequestMapping("/admin/")
public class AdminController {
    @Autowired
    private AdminService adminService;

    /**
     * 查询管理管理列表
     *
     * @param adminVo 查询条件
     * @return 返回管理员管理数据
     */
    @PostMapping("queryAdminList")
    public ResultVo<UserModel> queryAdminList(@RequestBody AdminVo adminVo) {
        return adminService.queryAdminList(adminVo);
    }

    /**
     * 删除usbKey 用户
     *
     * @param adminVo
     * @return 返回tru 成功   false 失败
     */
    @PostMapping("delUserKey")
    //@OperateAnnotation(operateModel = OperateMenuEnum.ADMIN_MANAGER, operateType = OperateTypeEnum.DEL_USBKEY)
    public ResultVo<Boolean> delUserKey(@RequestBody AdminVo adminVo) {
        return adminService.delUserKey(adminVo);
    }

    /**
     * 添加usbKey 用户
     *
     * @param userDto
     * @return 返回tru 成功   false 失败
     */
    @PostMapping("addUserKey")
    //@OperateAnnotation(operateModel = OperateMenuEnum.ADMIN_MANAGER, operateType = OperateTypeEnum.ADD_USBKEY)
    public ResultVo<Boolean> addUserKey(@RequestBody UserDto userDto) {
        return adminService.addUserKey(userDto);
    }

    /**
     * 修改密码
     *
     * @param adminVo 参数
     * @return
     */
    @PostMapping("updatePwd")
    //@OperateAnnotation(operateModel = OperateMenuEnum.ADMIN_MANAGER, operateType = OperateTypeEnum.UPDATE_USBKEY)
    public ResultVo<Boolean> updatePwd(@RequestBody UpdateAdminVo adminVo, HttpServletRequest httpServletRequest) {
        return adminService.updatePwd(adminVo,httpServletRequest);
    }
}
