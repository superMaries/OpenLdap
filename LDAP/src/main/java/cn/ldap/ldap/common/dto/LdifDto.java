package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * 导出文件参数
 *
 * @title: LdifDto
 * @Author Wy
 * @Date: 2023/4/11 9:01
 * @Version 1.0
 */
@Data
public class LdifDto {
    /**
     * 查询条件
     */
    private String baseDN;
    /**
     * 过滤条件
     */
    private String baseFilter;
    /**
     * 范围
     */
    private Integer scope;

    /**
     * 导出类型 0 服务器 1 本地
     */
    private Integer exportType;
    /**
     * 导出文件位置
     */
    private String exportFilePath;
}
