package cn.ldap.ldap.common.vo;

import lombok.Data;

@Data
public class NetstatVo {

    private String protocol;

    private String localAddress;

    private Integer port;

    private Integer pid;
}
