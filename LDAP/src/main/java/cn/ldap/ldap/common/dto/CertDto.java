package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: CertDto
 * @Author Wy
 * @Date: 2023/4/26 18:16
 * @Version 1.0
 */
@Data
public class CertDto {
    private String certBase64;
    /**
     * 0 证书 1 key
     */
    private Integer type;
}
