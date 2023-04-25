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
     * 范围 0 当前 1 一个条目 2 全部
     */
    private Integer scope;


    /**
     * 只导出分辨名
     */
    private boolean onlyRdn = false;
    /**
     * 导出类型 0 服务器 1 本地
     */
    private Integer exportType;
    /**
     * 导出文件位置
     */
    private String exportFilePath;

    /**
     * 属性名称
     */
    private String attribute;
    /**
     * 是否只返回属性名称
     */
    private boolean returnAttr = false;

    /**
     * 每页大小
     */
    private Integer pageSize = 1000;
    /**
     * 页码
     */
    private Integer page = 1;

}
