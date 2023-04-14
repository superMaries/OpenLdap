package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: ReBindTreDto
 * @Author Wy
 * @Date: 2023/4/14 16:07
 * @Version 1.0
 */
@Data
public class ReBindTreDto {
    /**
     * 唯一值 根据这个值查询
     */
    private String rdn;
    /**
     * 须修改的值
     */
    private String baseDn;
}
