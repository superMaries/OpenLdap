package cn.ldap.ldap.common.vo;

import lombok.Data;

/**
 * @title: CertTree
 * @Author Wy
 * @Date: 2023/4/11 9:32
 * @Version 1.0
 */
@Data
public class CertTreeVo {
    /**
     * 唯一值
     */
    private String rdn;
    /**
     * 展示的值
     */
    private String baseDn;
}
