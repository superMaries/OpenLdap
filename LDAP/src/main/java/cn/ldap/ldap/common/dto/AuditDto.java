package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * 审计员数据
 *
 * @title: AduitDto
 * @Author Wy
 * @Date: 2023/4/12 14:50
 * @Version 1.0
 */
@Data
public class AuditDto {
    /**
     * 日志id
     */
    public Integer id;
    /**
     * 审计员id
     */
    private Integer auditId;
    /**
     * 是否通过 0 不通过 1 通过
     */
    private Integer auditStatus;
    /**
     * 审计时间
     */
    private String auditTime;

    /**
     * 审计原数据
     */
    private String auditSrc;

    /**
     * 审计签名值
     */
    private String auditSignValue;

    /**
     * 备注
     */
    private String remark;

}
