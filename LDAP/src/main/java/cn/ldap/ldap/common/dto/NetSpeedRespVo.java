package cn.ldap.ldap.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title: NetSpeedRespVo
 * @Author Wy
 * @Date: 2023/3/31 11:23
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetSpeedRespVo {
    private String dateTime;
    private String upSpeed;
    private String  downSpeed;
}
