package cn.ldap.ldap.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
@Component
public class LdapConfig {
    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base}")
    private String ldapBase;

    @Value("${ldap.userDn}")
    private String ldapUserDn;

    @Value("${ldap.password}")
    private String ldapPassword;

    @Value("${ldap.pool.minIdle}")
    private int ldapPoolMinIdle;

    @Value("${ldap.pool.maxIdle}")
    private int ldapPoolMaxIdle;

    @Value("${ldap.pool.maxActive}")
    private int ldapPoolMaxActive;

    @Value("${ldap.pool.maxWait}")
    private int ldapPoolMaxWait;

    @Autowired
    private PooledContextSource pooledContextSource;


    @Bean
    public ContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapUserDn);
        contextSource.setPassword(ldapPassword);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public PoolConfig poolConfig() {
        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setMinIdlePerKey(ldapPoolMinIdle);
        poolConfig.setMaxIdlePerKey(ldapPoolMaxIdle);
        poolConfig.setMaxTotal(ldapPoolMaxActive);
        poolConfig.setMaxWaitMillis(ldapPoolMaxWait);
        return poolConfig;
    }

    @Bean
    public DirContextValidator dirContextValidator() {
        return new DefaultDirContextValidator();
    }

    @Bean
    public PooledContextSource pooledContextSource(ContextSource contextSource, PoolConfig poolConfig, DirContextValidator dirContextValidator) {
        PooledContextSource pooledContextSource = new PooledContextSource(poolConfig);
        pooledContextSource.setContextSource(contextSource);
        pooledContextSource.setDirContextValidator(dirContextValidator);
        return pooledContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(PooledContextSource pooledContextSource) {
        return new LdapTemplate(pooledContextSource);
    }
    //------------




    @PostConstruct
    public void printPoolInfo() {
        System.out.println("Max idle connections per key: " + pooledContextSource.getPoolConfig().getMaxIdlePerKey());
        System.out.println("Max total connections: " + pooledContextSource.getPoolConfig().getMaxTotal());
        System.out.println("Max wait time for a connection: " + pooledContextSource.getPoolConfig().getMaxWaitMillis());
        System.out.println("Number of active connections: " + pooledContextSource.getNumActive());
        System.out.println("Number of idle connections: " + pooledContextSource.getNumIdle());
    }

}
