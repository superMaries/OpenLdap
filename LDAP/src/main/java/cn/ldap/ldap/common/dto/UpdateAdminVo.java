package cn.ldap.ldap.common.dto;


import lombok.Data;

/**
 * @title: UpdateAdminVo
 * @Author Wy
 * @Date: 2023/4/10 17:40
 * @Version 1.0
 */
@Data
public class UpdateAdminVo {
    private String oldPassword;
    private String password;
}
