package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @title: KeyModel
 * @Author Wy
 * @Date: 2023/4/20 11:14
 * @Version 1.0
 */
@TableName("key")
@Data
public class KeyModel {
    private Integer id;
    /**
     * 私钥
     */
    @TableField("private_key")
    private String privateKey;
    /**
     * 公钥
     */
    @TableField("public_key")
    private String publicKey;

}
