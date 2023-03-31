package cn.ldap.ldap.common.dto;

import lombok.Data;

/**
 * @title: UserDto
 * @Author Wy
 * @Date: 2023/3/31 10:45
 * @Version 1.0
 */
@Data
public class UserDto extends SrcModel {
      private String signCert;
      private String certSn;
      /**
       * 0, "主服务器"
       * 1, "从服务器"
       */
      private Integer serviceType;
}
