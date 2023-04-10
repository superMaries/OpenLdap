package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * 管理员管理查询DTO
 *
 * @title: AdminVo
 * @Author Wy
 * @Date: 2023/4/10 14:05
 * @Version 1.0
 */
@Data
public class AdminVo {

    private Integer id;
    /**
     * 证书名称
     */
    private String certName;
    /**
     * 证书序列号
     */
    private String sertNo;
}
