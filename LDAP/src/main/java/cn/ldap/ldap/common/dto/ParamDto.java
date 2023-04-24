package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class ParamDto {

    private String fileName;
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
    private Integer pageSize = 1000;
    private Integer page=1;

    private Boolean webOrFile;
}
