package cn.ldap.ldap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.ldap.ldap.common.mapper")
public class LdapApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdapApplication.class, args);
    }

}
