package cn.ldap.ldap.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @title: CertVo
 * @Author Wy
 * @Date: 2023/4/26 17:59
 * @Version 1.0
 */

@Data
public class CertVo {


    /**
     * 证书序列号
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String certNum;
    /**
     * 证书算法
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String certAlgorithm;
    /**
     * 颁发者名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String certIssName;

    /**
     * 使用者名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String certSubName;

    /**
     * 开始时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String startTime;
    /**
     * 结束时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String endTime;

    /**
     * 设备号
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deviceNo;

    /**
     * 文件内容
     */
    private String baseStr;
}
