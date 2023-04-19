package cn.ldap.ldap.common.vo;

import lombok.Data;

/**
 * @title: TreeVo
 * @Author Wy
 * @Date: 2023/4/11 10:45
 * @Version 1.0
 */
@Data
public class TreeVo {
    /**
     * 属性
     */
    private String key;
    /**
     * 属性值
     */
    private String  value;

    /**
     * 显示的值
     */
    private String title;

    private boolean flag = false;
}
