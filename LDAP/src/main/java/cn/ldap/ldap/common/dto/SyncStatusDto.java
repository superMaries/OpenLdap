package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Data
public class SyncStatusDto {

    private Integer id;

    private String url;

    private String account;

    private String password;

    private String syncPoint;
}
