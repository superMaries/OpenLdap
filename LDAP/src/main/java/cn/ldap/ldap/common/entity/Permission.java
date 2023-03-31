package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permission")
public class Permission {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 归属菜单,前端判断并展示菜单使用,
     */
    @TableField("menu_code")
    private String menuCode;
    /**
     * 菜单的中文释义
     */
    @TableField("menu_name")
    private String menuName;
    /**
     * 权限的代码/通配符,对应代码中@RequiresPermissions 的value
     */
    @TableField("permission_code")
    private String permissionCode;
    /**
     * 本权限的中文释义
     */
    @TableField("permission_name")
    private String permissionName;
    /**
     * 前端图标
     */
    @TableField("icon")
    private String icon;


    @TableField("parent_id")
    private String parentId;
}
