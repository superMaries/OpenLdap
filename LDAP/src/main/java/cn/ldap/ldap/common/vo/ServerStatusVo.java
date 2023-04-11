package cn.ldap.ldap.common.vo;

import lombok.Data;

@Data
public class ServerStatusVo {

    private String serverName;

    private String port;

    private Boolean status;
}
