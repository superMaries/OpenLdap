package cn.ldap.ldap.common.dto;

import lombok.Data;

@Data
public class LogDto {

    private String beginTime;

    private String endTime;

    private String operateType;

    private Integer pageNum;

    private Integer pageSize;
}
