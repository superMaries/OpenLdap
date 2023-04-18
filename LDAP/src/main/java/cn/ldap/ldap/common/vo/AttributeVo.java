package cn.ldap.ldap.common.vo;

import lombok.Data;

import javax.naming.directory.Attributes;
import java.util.List;

/**
 * @title: AttributeVo
 * @Author Wy
 * @Date: 2023/4/18 10:49
 * @Version 1.0
 */
@Data
public class AttributeVo {
    private String rdn;
    private List<Attributes> attributes;
}
