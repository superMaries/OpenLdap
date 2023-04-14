package cn.ldap.ldap.common.dto;

import cn.ldap.ldap.common.vo.TreeVo;
import lombok.Data;

import java.util.List;

/**
 * @title: LdapBindTreedTO
 * @Author Wy
 * @Date: 2023/4/14 13:50
 * @Version 1.0
 */
@Data
public class LdapBindTreeDto {
    /**
     * 查询的值
     */
    private String rdn;
    /**
     * 需要修改属性的值
     */
    private List<TreeVo> attributes;
}
