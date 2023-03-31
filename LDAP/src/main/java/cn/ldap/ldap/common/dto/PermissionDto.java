package cn.ldap.ldap.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionDto {

    /**
     * 主键
     */
    private Integer id;
    /**
     * 菜单编码
     */
    private String menuCode;
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 图标
     */
    private String icon;
    /**
     * 父级ID
     */
    private Integer parentId;

    List<PermissionDto> permissionDtoList;
}
