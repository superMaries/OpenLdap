package cn.ldap.ldap.common.entity;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

public class TestTotal {
    public static void main(String[] args) {
        String ldapUrl = "ldap://123.57.204.10:389"; // LDAP服务的URL
        String ldapUserDn = "cn=Directory Manager,c=cn"; // LDAP管理员的DN
        String ldapPassword = "Js3qCaLdapAdmin"; // LDAP管理员的密码
        String baseDn = "c=cn"; // 要查询数据的基础DN
        String filter = "(serialNumber=*)"; // 查询过滤器

        try {
            // 创建连接
            String ldapContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
            String ldapSecurityAuthentication = "simple";
            String ldapReferral = "follow";
            String ldapBindDn = ldapUserDn;
            String ldapBindPassword = ldapPassword;
            String ldapVersion = "3";
            String ldapTimeout = "5000";

            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContextFactory);
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, ldapSecurityAuthentication);
            env.put(Context.REFERRAL, ldapReferral);
            env.put(Context.SECURITY_PRINCIPAL, ldapBindDn);
            env.put(Context.SECURITY_CREDENTIALS, ldapBindPassword);
            env.put("java.naming.ldap.version", ldapVersion);
            env.put("com.sun.jndi.ldap.connect.timeout", ldapTimeout);

            DirContext ctx = new InitialDirContext(env);

            // 查询数据
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[] { "serialNumber" });
            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, searchControls);

            // 统计RDN总数
            int count = 0;
            while (results.hasMore()) {
                SearchResult result = results.next();
                count++;
            }

            // 关闭连接
            ctx.close();

            System.out.println("Total count: " + count);
        } catch (NamingException e) {
            System.out.println("Search failed.");
            e.printStackTrace();
        }
    }
}
