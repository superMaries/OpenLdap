package cn.ldap.ldap.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * @title: PermissionVo
 * @Author Wy
 * @Date: 2023/4/13 17:00
 * @Version 1.0
 */
@Data
public class PermissionVo {
    /**
     *
     */
    private Integer id;
    /**
     * 菜单的路由
     */
    private String menuPath;
    /**
     * 菜单的中文释义
     */
    private String menuName;
    /**
     * 前端图标
     */
    private String icon;


    private String parentId;

    private List<PermissionVo> children;
}
