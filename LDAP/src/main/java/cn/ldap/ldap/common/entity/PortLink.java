package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("port_link")
public class PortLink {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("port")
    private String port;

    @TableField("server_name")
    private String serverName;

    @TableField("status")
    private String status;


}
