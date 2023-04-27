package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("index_rule")
public class IndexRule {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    @TableField(value = "type")
    private String type;

    @TableField(value = "description")
    private String description;
}
