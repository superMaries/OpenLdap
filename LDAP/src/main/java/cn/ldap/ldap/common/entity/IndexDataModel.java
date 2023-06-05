package cn.ldap.ldap.common.entity;

import cn.ldap.ldap.service.IndexDataService;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.lang.model.util.TypeKindVisitor6;

/**
 * @title: IndexDataModel
 * @Author Wy
 * @Date: 2023/4/11 17:35
 * @Version 1.0
 */
@Data
@TableName("index_data")
public class IndexDataModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 索引
     */
    @TableField("index_attribute")
    private String indexAttribute;
    /**
     * 规则
     */
    @TableField("index_rule")
    private String indexRule;
    /**
     * 描述
     */
    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;
}
