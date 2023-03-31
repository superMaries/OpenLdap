package cn.ldap.ldap.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title: ResultVo
 * @Author Wy
 * @Date: 2023/3/31 16:51
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVo<T> {
    private Integer code;
    private String  message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}