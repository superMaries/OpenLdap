package cn.ldap.ldap.common.entity;

import cn.ldap.ldap.common.vo.PermissionVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@TableName("permission")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 菜单的路由
     */
    @TableField("menu_path")
    private String menuPath;
    /**
     * 菜单的中文释义
     */
    @TableField("menu_name")
    private String menuName;


    /**
     * 前端图标
     */
    @TableField("icon")
    private String icon;


    @TableField("parent_id")
    private Integer parentId;

    @JsonIgnore
    @TableField("role_id")
    private Integer roleId;

    @TableField(exist = false)
    private List<Permission> children;
}
