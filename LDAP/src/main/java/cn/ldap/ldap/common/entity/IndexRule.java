package cn.ldap.ldap.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("index_rule")
public class IndexRule {

    private Integer id;

    private String type;

    private String description;
}
