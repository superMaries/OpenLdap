package cn.ldap.ldap.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @title: LdapAccountVo
 * @Author Wy
 * @Date: 2023/5/8 10:03
 * @Version 1.0
 */
@Data
public class LdapAccountVo {
    private String account;

    @JsonIgnore
    private String pwd;
    /**
     * 1 读
     * 2 写
     * 3 读写
     */
    private String auth;
}
