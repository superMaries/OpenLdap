package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Data
@TableName("param_config")
public class ParamConfig {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    @TableField("log_level")
    private Integer logLevel;

    @TableField("log_file")
    private String logFile;


    @TableField("open_acl")
    private String openAcl;

    /**
     * 日志文件大小
     */
    @TableField("log_size")
    private Integer logSize;
}
