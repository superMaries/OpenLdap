package cn.ldap.ldap.common.vo;

import cn.ldap.ldap.common.util.StaticValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @title: LogVo
 * @Author Wy
 * @Date: 2023/4/12 14:46
 * @Version 1.0
 */
@Data
public class LogVo {
    private Integer id;
    private String clientIp;
    /**
     * 操作用户Id
     */
    private Integer userId;
    /**
     * 操作用户名称
     */
    private Integer userName;
    /**
     * 操作模块
     */
    private String operateMenu;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 操作对象
     */
    private String operateObject;
    /**
     * 1 成功 0 失败
     * 操作状态
     */
    @JsonIgnore
    private Integer operateState;
    /**
     * 操作状态
     */
    private String operateStateName;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 管理员验签结果
     */
    private String adminVerify;

    /**
     * 审计员Id
     */
    private Integer auditId;
    /**
     * 审计员名称
     */
    private String auditName;
    /**
     * 审计时间
     */
    private String auditTime;
    /**
     * 是否审计 0 未审计 1 审计
     */
    private Integer auditStatus;
    /**
     * 审计
     */
    private String auditStatusName;
    /**
     * 审计员验签结果
     */
    private String auditVerify;
    /**
     * 备注
     */
    private String remark;

    /**
     * 签名原数据
     */
    @JsonIgnore
    private String signSrc;
    /**
     * 签名值
     */
    @JsonIgnore
    private String signVlue;
    /**
     * 签名证书
     */
    @JsonIgnore
    private String signCert;

    /**
     * 审计签名原数据
     */
    @JsonIgnore
    private String auditSrc;
    /**
     * 签名值
     */
    @JsonIgnore
    private String auditSignValue;

    @JsonIgnore
    private String failCode;

    /**
     * 操作原数据
     * 时间 + | +模块  -  操作 + 谁 + 编码
     *
     * @return 返回数据
     */
    public String operateSrcToString() {
        return this.getCreateTime() + StaticValue.VERTICAL
                + this.getOperateMenu()
                + StaticValue.LINE + this.getOperateType()
                + this.getUserName()
                + StaticValue.VERTICAL + this.getFailCode();
    }

    /**
     * 审计原数据
     * 时间 + | + 日志管理-审计日志 +| + 谁 + 审计结果(审计状态) -备注
     *
     * @return
     */
    public String auditSrcToString() {
        return this.getAuditName() + StaticValue.VERTICAL
                + StaticValue.LOG
                + StaticValue.VERTICAL
                + this.getAuditName()
                + StaticValue.VERTICAL
                + this.getAuditStatus()
                + StaticValue.LINE
                + this.remark;
    }
}
