package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * 树形结构DTO
 *
 * @title: CertTreeVo
 * @Author Wy
 * @Date: 2023/4/11 9:03
 * @Version 1.0
 */
@Data
public class CertTreeDto {
    /**
     * 查询条件
     */
    private String baseDN="";
    /**
     * 过滤条件
     */
    private String filter="";
    /**
     * 属性名称
     */
    private String attribute;
    /**
     * 是否只返回属性名称
     */
    private boolean returnAttr = false;
    /**
     * 查询范围
     */
    private Integer scope = 0;
    /**
     * 每页大小
     */
    private Integer pageSize = 50;

}
