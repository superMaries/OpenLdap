package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/**
 * @title: UserModel
 * @Author Wy
 * @Date: 2023/3/31 9:19
 * @Version 1.0
 */
@Data
@TableName("User")
public class UserModel {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 角色
     */
    @TableField("role_id")
    private Integer roleId;
    /**
     * 证书序列号
     */
    @TableField("cert_sn")
    private String certSn;
    /**
     * 签名证书
     */
    @TableField("sign_cert")
    private String signCert;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;
    /**
     * 是否禁用
     */
    @TableField("is_enable")
    private Integer isEnable;
}
