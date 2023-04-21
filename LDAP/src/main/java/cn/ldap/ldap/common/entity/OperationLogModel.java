package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title: OperationLog
 * @Author Wy
 * @Date: 2023/3/31 16:03
 * @Version 1.0
 */
@Data
@TableName("operation_log")
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 访问
     */
    @TableField("client_ip")
    private String clientIp;
    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 操作菜单
     */
    @TableField("operate_menu")
    private String operateMenu;

    /**
     * 操作类型
     */
    @TableField("operate_type")
    private String operateType;

    /**
     * 操作对象
     */
    @TableField("operate_object")
    private String operateObject;

    /**
     * 操作状态（成功,失败）
     */
    @TableField("operate_state")
    private Integer  operateState;
    /**
     * 编码
     */
    @TableField("fail_code")
    private String failCode  ;
    /**
     * 签名值
     */
    @TableField("sign_value")
    private String signValue;

    /**
     * 签名原数据
     */
    @TableField("sign_src")
    private String signSrc;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private String createTime;

    /**
     * 审计员Id
     */
    @TableField("audit_id")
    private Integer auditId;
    /**
     * 是否审计 0 未审计 1 审计
     */
    @TableField("audit_status")
    private Integer auditStatus;
    /**
     * 0 未通过 1 通过
     */
    @TableField("pass")
    private Integer pass ;

    /**
     * 审计时间
     */
    @TableField("audit_time")
    private String auditTime;
    /**
     * 审计原数据
     */
    @TableField("audit_src")
    private String auditSrc;

    /**
     * 审计签名值
     */
    @TableField("audit_sign_value")
    private String auditSignValue;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}
