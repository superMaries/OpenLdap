package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Data
@TableName("sync_status")
public class SyncStatus {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 从服务器IP地址
     */
    @TableField("follow_server_ip")
    private String followServerIp;
    /**
     * 同步节点
     */
    @TableField("sync_point")
    private String syncPoint;
    /**
     * 主服务数量
     */
    @TableField("main_server_number")
    private String mainServerNumber;
    /**
     * 从服务数量
     */
    @TableField("follow_server_number")
    private String followServerNumber;
    /**
     * 同步状态
     */
    @TableField("sync_status")
    private String syncStatusStr;
    /**
     * 账号
     */
    @TableField("account")
    private String account;
    /**
     * 密码
     */
    @TableField("password")
    private String password;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;
    /**
     * 修改时间
     */
    @TableField("update_time")
    private String updateTime;
}
