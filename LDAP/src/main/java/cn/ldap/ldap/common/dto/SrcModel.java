package cn.ldap.ldap.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class SrcModel {
    /**
     * 签名值
     */
    @TableField(exist = false)
    private String signData;

    /**
     * 原始数据
     */
    @TableField(exist = false)
    private  String signOriginalData;
}
