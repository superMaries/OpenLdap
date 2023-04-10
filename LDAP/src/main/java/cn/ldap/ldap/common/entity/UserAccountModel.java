package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * admin 实体类
 *
 * @title: UserAccountModel
 * @Author Wy
 * @Date: 2023/4/10 16:09
 * @Version 1.0
 */
@Data
@TableName("user_account")
public class UserAccountModel {
    private Integer id;
    private String account;
    private String password;
}
