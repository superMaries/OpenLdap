package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @title: Configf
 * @Author Wy
 * @Date: 2023/3/31 9:38
 * @Version 1.0
 */
@Data
@TableName("config")
public class ConfigModel {
    /**
     * 自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * key值
     */
    @TableField("code")
    private String  code;
    /**
     * 0, "主服务器"
     * 1, "从服务器"
     * 是否展示同步
     * 0 不展示同步
     * 1 展示同步
     */
    @TableField("service_type")
    private Integer serviceType;
}
