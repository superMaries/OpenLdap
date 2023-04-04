package cn.ldap.ldap.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @title: ReadOnlyVo
 * @Author Wy
 * @Date: 2023/4/3 11:31
 * @Version 1.0
 */

public class ReadOnlyVo {
    /**
     * 标记该实体中有多少元素
     */

    private  long elementCount=3;

    public long getElementCount() {
        return elementCount;
    }
}
