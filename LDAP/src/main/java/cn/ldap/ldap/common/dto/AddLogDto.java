package cn.ldap.ldap.common.dto;

import lombok.Data;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 日志实体
 *
 * @title: AddLogDto
 * @Author Wy
 * @Date: 2023/4/12 14:21
 * @Version 1.0
 */
@Data
public class AddLogDto extends AuditDto {
    private String clientIp;
    /**
     * 操作用户Id
     */
    private Integer userId;
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
    private Integer operateStatus;
    /**
     * 返回的编码
     */
    private String failCode;
    /**
     * 创建时间
     */
    private String createTime;


    /**
     * 签名值
     */
    private String signValue;

    /**
     * 签名原数据
     */
    private String signSrc;


}
