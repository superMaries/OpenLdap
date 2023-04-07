package cn.ldap.ldap.common.util;

import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

/**
 * @title: LdapUtil
 * @Author Wy
 * @Date: 2023/4/4 9:16
 * @Version 1.0
 */
@Slf4j
public class LdapUtil {

    public static long total(LdapTemplate ldapTemplate, String ldapSearchFilter, String ldapSearchBase) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        int totalNodeCount = 0;
        int pageSize = 1000;
        try {
            Control[] controls = new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)};
            ctx.setRequestControls(controls);

            byte[] cookie = null;
            do {
                NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter, searchControls);

                while (results.hasMore()) {
                    SearchResult result = results.next();
                    totalNodeCount++;
                }

                Control[] responseControls = ctx.getResponseControls();
                if (responseControls != null) {
                    for (Control control : responseControls) {
                        if (control instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
                            cookie = prrc.getCookie();
                        }
                    }
                }

                ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
            } while (cookie != null);
            ctx.close();
        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            return totalNodeCount;
        }
        return totalNodeCount;
    }

    public static long queryTotal(LdapTemplate ldapTemplate, String ldapSearchFilter, String ldapSearchBase, String... whereParam) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        int totalNodeCount = 0;
        int pageSize = StaticValue.LDAP_PAGE_SIZE;
        try {
            //设置每页查询的数量
            Control[] controls = new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)};
            ctx.setRequestControls(controls);

            byte[] cookie = null;
            do {
                //分页查询
                NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter, searchControls);
                //统计总数
                while (results.hasMore()) {
                    SearchResult result = results.next();
                    String name = result.getName();
                    if (ObjectUtils.isEmpty(whereParam)) {
                        totalNodeCount++;
                    } else {
                        for (int i = 0; i < whereParam.length; i++) {
                            if (name.startsWith(whereParam[i])) {
                                totalNodeCount++;
                            }
                        }
                    }
                }
                Control[] responseControls = ctx.getResponseControls();
                //设置Cookies
                if (responseControls != null) {
                    for (Control control : responseControls) {
                        if (control instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
                            cookie = prrc.getCookie();
                        }
                    }
                }
                ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
            } while (cookie != null);
            ctx.close();
        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            return totalNodeCount;
        }
        return totalNodeCount;
    }

    public static long funTotal(LdapTemplate ldapTemplate, String base, long size, long count, String... whereParam) {
        if (size == 0) {
            return count;
        }
        List<String> tempList = null;
        try {
            tempList = ldapTemplate.list(base);
        } catch (Exception e) {
            return count;
        }
        for (String temp : tempList) {
            count++;
            String tempBase = temp + base;
            funTotal(ldapTemplate, tempBase, tempList.size(), 0);

        }
        return count;
    }


    /**
     * @param base       父级
     * @param size       查询的大小
     * @param count      返回的总数
     * @param whereParam 查询的条件
     * @return
     */
    public static long fun(LdapTemplate ldapTemplate, String base, long size, long count, String... whereParam) {
        for (int i = 0; i < whereParam.length; i++) {
            if (base.startsWith(whereParam[i])) {
                count += size;
                return count;
            }
        }
        List<String> tempList = ldapTemplate.list(base);
        for (String temp : tempList) {
            boolean isCount = false;
            for (int i = 0; i < whereParam.length; i++) {
                if (temp.startsWith(whereParam[i])) {
                    isCount = true;
                    count++;
                }
            }
            if (!isCount) {
                String tempBase = temp + "," + base;
                long fun = fun(ldapTemplate, tempBase, tempList.size(), 0, whereParam);
                count = count + fun;
            }
        }
        return count;
    }
}
