package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: IndexDataDto
 * @Author Wy
 * @Date: 2023/4/11 17:29
 * @Version 1.0
 */
@Data
public class IndexDataDto {
    private Integer id;
    private String attributeName;
    private String indexRule;
}
