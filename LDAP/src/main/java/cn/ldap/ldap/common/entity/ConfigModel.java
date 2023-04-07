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
     * 是否初始化
     * 0 未初始化
     * 1 初始化
     */
    @TableField("is_init")
    private Integer isInit;
    /**
     * 0, "主服务器"
     * 1, "从服务器"
     */
    @TableField("service_type")
    private Integer serviceType;
}
