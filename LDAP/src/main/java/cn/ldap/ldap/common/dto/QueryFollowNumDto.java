package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @author suntao
 * @create 2023/5/23
 */
@Data
public class QueryFollowNumDto {

    private String url;

    private String syncPoint;

    private String userName;

    private String password;

    private Long mainCount;
}
