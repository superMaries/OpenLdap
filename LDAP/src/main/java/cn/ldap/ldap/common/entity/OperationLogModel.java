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
    private Integer operateState;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private String createTime;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 签名原数据
     */
    @TableField("src")
    private String src;

    /**
     * 签名值
     */
    @TableField("sign_data")
    private String signData;

    /**
     * 审计状态,1通过，2.未通过
     */
    @TableField("audit_status")
    private Integer auditStatus;
    /**
     * 审计人员签名数据
     */
    @TableField("audit_sign_data")
    private String auditSignData;

    /**
     * 审计用户id
     */
    @TableField("audit_user_id")
    private Integer auditUserId;

    @TableField("operate_verity_state")
    private Integer operateVerityState;


    @TableField("audit_time")
    private String auditTime;
    /**
     * 审计备注
     */
    @TableField("auth_remark")
    private String authRemark;
}
