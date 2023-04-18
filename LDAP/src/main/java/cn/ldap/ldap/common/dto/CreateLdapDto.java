package cn.ldap.ldap.common.dto;

import lombok.Data;

import java.util.List;

/**
 * @title: CerateLdapDto
 * @Author Wy
 * @Date: 2023/4/18 13:06
 * @Version 1.0
 */
@Data
public class CreateLdapDto {
    /**
     * rdn
     */
    private String rdn;
    private List<CreateAttDto> createAttDtos;
}
