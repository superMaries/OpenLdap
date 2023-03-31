package cn.ldap.ldap.common.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVo {
    /**
     * token
     */
    @JsonUnwrapped
    private String authorization;

    /**
     * 用户信息
     */
    @JsonUnwrapped
    private UserRedisInfo userInfo;
}
