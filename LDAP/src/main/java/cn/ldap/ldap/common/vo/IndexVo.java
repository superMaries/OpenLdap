package cn.ldap.ldap.common.vo;

import com.sun.org.glassfish.external.statistics.annotations.Reset;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @title: IndexVo
 * @Author Wy
 * @Date: 2023/4/3 9:37
 * @Version 1.0
 */
@Data
public class IndexVo {
    private long total;
    private long certTotal;
    private long crlTotal;
}
