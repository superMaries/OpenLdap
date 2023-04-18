package cn.ldap.ldap.common.dto;

import cn.hutool.db.DaoTemplate;
import lombok.Data;

import java.util.List;

/**
 * @title: CreateAttDto
 * @Author Wy
 * @Date: 2023/4/18 13:18
 * @Version 1.0
 */
@Data
public class CreateAttDto {
    /**
     * 属性
     */
    private String key;
    /**
     * 属性值
     */
    private List<String> values;
}
