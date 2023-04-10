package cn.ldap.ldap.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserTokenInfo {
    /**
     * id为 0 表示admin 登录 其余是usebKey登陆
     */
    private Integer id;
    private Integer roleId;

    private String certData;

    private String certName;

    private String certNum;

    private String roleName;
    @JsonProperty("TOKEN")
    private String token;

}
