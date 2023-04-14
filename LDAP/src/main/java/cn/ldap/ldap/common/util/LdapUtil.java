package cn.ldap.ldap.common.util;

import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.dto.LdapBindTreeDto;
import cn.ldap.ldap.common.dto.LdapDto;
import cn.ldap.ldap.common.dto.ReBindTreDto;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.LdapAttribute;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.ObjectUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.*;
import java.util.stream.Collectors;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

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
            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, Control.CRITICAL)};
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

                ctx.setRequestControls(new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);
            ctx.close();
        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            return totalNodeCount;
        }
        return totalNodeCount;
    }

    /**
     * 查询总数 CRL cert 节点总数
     *
     * @param ldapTemplate     ldap 连接模板
     * @param ldapSearchFilter 过滤条件
     * @param ldapSearchBase   查询条件
     * @param whereParam       查询总数的条件
     * @return
     */
    public static Long queryTotal(LdapTemplate ldapTemplate, String ldapSearchFilter, String ldapSearchBase, String... whereParam) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        Long totalNodeCount = StaticValue.TOTAL_NODE_NUM;
        int pageSize = StaticValue.LDAP_PAGE_SIZE;
        try {
            //设置每页查询的数量
            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, Control.CRITICAL)};
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
                ctx.setRequestControls(new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);
            ctx.close();
        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            return totalNodeCount;
        }
        return totalNodeCount;
    }

    /**
     * @param ldapTemplate ldap 查询模板
     * @param baseDN       查询条件
     * @param isRetrunAttr 是否返回属性值
     * @param attribute    属性值
     * @return 返回查询节点属性详情
     */
    public static List<TreeVo> queryAttributeInfo(LdapTemplate ldapTemplate, String baseDN, boolean isRetrunAttr, String attribute) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        List<TreeVo> treeVos = new ArrayList<>();
        Attributes attributes = null;
        // 对rdn 进行分析出来
        String parentStr = baseDN.split(StaticValue.SPLIT)[0];
        String[] addStr = parentStr.split(StaticValue.ADD);
        List<String> list = Arrays.asList(addStr);

        try {
            //查询对应的属性值
            if (isRetrunAttr && !ObjectUtils.isEmpty(attribute)) {
                attributes = ctx.getAttributes(baseDN, attribute.split(StaticValue.SPLIT));
            } else {
                attributes = ctx.getAttributes(baseDN);
            }
            NamingEnumeration<? extends Attribute> attributesAll = attributes.getAll();
            //解析属性值
            while (attributesAll.hasMore()) {
                Attribute next = attributesAll.next();
                String key = next.getID();
                NamingEnumeration<?> keyAll = next.getAll();
                while (keyAll.hasMore()) {
                    TreeVo treeVo = new TreeVo();
                    String attrValue = keyAll.nextElement().toString();

                    List<String> collect = list.stream().filter(it -> it.equals(key + StaticValue.EQ + attrValue)).collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(collect.size()) && collect.size() >= StaticValue.COUNT) {
                        treeVo.setFlag(StaticValue.TRUE);
                    } else if (StaticValue.OBJECTA_CLASS.toUpperCase().equals(key.toUpperCase().trim())) {
                        treeVo.setFlag(StaticValue.TRUE);
                    }
                    treeVo.setKey(key);
                    treeVo.setValue(attrValue);
                    treeVos.add(treeVo);
                }
            }
        } catch (NamingException e) {
            log.error("ldap 查询属性异常:{}", e.getMessage());
            return treeVos;
        }
        return treeVos;
    }

    /**
     * @param ldapTemplate     ldap 查询模板
     * @param ldapSearchFilter 过滤条件
     * @param ldapSearchBase   查询条件
     * @param scope            范围   one`：搜索指定的DN及其一级子节点。`sub`：搜索指定的DN及其所有子孙节点。
     * @param pageSize         条数
     * @return 返回节点数
     */
    public static List<CertTreeVo> queryCertTree(LdapTemplate ldapTemplate, String ldapSearchFilter,
                                                 String ldapSearchBase, Integer scope, Integer pageSize) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(scope);
        //查询多少条
        searchControls.setCountLimit(pageSize);
        int totalNodeCount = 0;
        List<CertTreeVo> certTreeVos = new ArrayList<>();
        try {
            //设置每页查询的数量
            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, Control.CRITICAL)};
            ctx.setRequestControls(controls);

            byte[] cookie = null;

            do {
                //分页查询
                NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter, searchControls);
                //统计总数
                while (results.hasMore()) {
                    CertTreeVo certTreeVo = new CertTreeVo();
                    SearchResult result = results.next();
                    String baseDn = result.getName();
                    if (ObjectUtils.isEmpty(baseDn)) {
                        String[] split = ldapSearchBase.split(StaticValue.SPLIT);
                        baseDn = split[0];
                    }
                    certTreeVo.setBaseDn(baseDn);

                    String newName = baseDn.split(StaticValue.SPLIT)[StaticValue.SPLIT_COUNT];
                    try {
                        String fullName=result.getNameInNamespace();
                        String parentRdn=fullName.replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
                        //设置父级的RDN
                        certTreeVo.setParentRdn(parentRdn);
                        certTreeVo.setRdn(fullName);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        certTreeVo.setRdn(certTreeVo.getBaseDn());
                        String parentRdn=certTreeVo.getBaseDn().replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
                        //设置父级的RDN
                        certTreeVo.setParentRdn(parentRdn);
                    }
                    certTreeVos.add(certTreeVo);
                }
                //获取最近一次 LDAP 操作的响应控制器。
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
                ctx.setRequestControls(new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);
            ctx.close();
        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            return certTreeVos;
        }
        return certTreeVos;
    }

    /**
     * @param ldapTemplate 查询模板
     * @param scope        查询范围
     * @param baseDN       查询条件
     * @param filter       过滤条件
     * @return
     */
    public static Map<String, Object> queryTreeRdnOrNum(Map<String, Object> map, LdapTemplate ldapTemplate, Integer scope, String baseDN, String filter) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(scope);

        int pageSize = StaticValue.LDAP_PAGE_SIZE;

        map.put(StaticValue.RDN, baseDN);
        // 执行搜索操作
        NamingEnumeration<SearchResult> searchResults = null;
        try {
            //设置分页条件
            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, Control.CRITICAL)};
            ctx.setRequestControls(controls);
            byte[] cookie = null;

            Long totalNodeCount = StaticValue.TOTAL_NODE_NUM;
            Long total = StaticValue.TOTAL_NODE_NUM;

            do {
                searchResults = ctx.search(baseDN, filter, searchControls);
                while (searchResults.hasMore()) {
                    SearchResult next = searchResults.next();
                    String name = next.getName();
                    String fullName = next.getNameInNamespace();
                    if (!StaticValue.REPLACE.equals(name)) {
                        //取DN
                        String newName = name.split(StaticValue.SPLIT)[StaticValue.SPLIT_COUNT];
                        newName = fullName.replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
                        if (baseDN.equals(newName)) {
                            total++;
                        }
                    }
                    totalNodeCount++;
                    log.info("节点 {} 中的条目数为：{}", baseDN, total);
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
                ctx.setRequestControls(new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);

            map.put(StaticValue.RDN_NUM_KEY, total);
            map.put(StaticValue.RDN_CHILD_NUM_KEY, totalNodeCount);
            return map;
        } catch (NamingException | IOException e) {
            log.info("节点 {} 不存在", e.getMessage());
            throw new SystemException(ExceptionEnum.QUERY_POINT_ERROR);
        } finally {
            // 关闭LDAP连接
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                log.info("节点 {} 不存在", e.getMessage());
                throw new SystemException(ExceptionEnum.QUERY_POINT_ERROR);
            }
        }
    }

    /**
     * 删除Ldap
     *
     * @param ldapDto 参数
     * @return true 成功 false 失败
     */
    public static boolean delLdapTreByRdn(LdapTemplate ldapTemplate, LdapDto ldapDto, String filter) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        //根据条件查询节点并删除子节点
        return queryChildRdn(ldapDto.getRdn(), filter, ctx);
    }

    /**
     * 编辑属性
     *
     * @param ldapTemplate     查询模板
     * @param ldapBindTreeDto  参数
     * @param ldapSearchFilter 过滤
     * @return true 成功 false 失败
     */
    public static boolean updateLdapBindTree(LdapTemplate ldapTemplate, LdapBindTreeDto ldapBindTreeDto, String ldapSearchFilter) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        try {
            String rdn = ldapBindTreeDto.getRdn();
            Attributes attributes = ctx.getAttributes(rdn);
            NamingEnumeration<? extends Attribute> attributesAll = attributes.getAll();
            List<TreeVo> attributeList = ldapBindTreeDto.getAttributes();
            List<String> oldAtt = new ArrayList<>();
            while (attributesAll.hasMore()) {
                Attribute next = attributesAll.next();
                String key = next.getID();
                //查询到key对应的values 的值
                List<String> keys = attributeList.stream()
                        .filter(it -> it.getKey().equals(key))
                        .map(it -> it.getValue())
                        .collect(Collectors.toList());
                next.clear();
                for (String value : keys) {
                    next.add(value);
                    //移除已经修改的数据
                    TreeVo vo = new TreeVo();
                    vo.setValue(value);
                    vo.setKey(key);
                    attributeList.remove(vo);
                }
                //获取以及添加的key
                oldAtt.add(key);
                ctx.modifyAttributes(rdn, new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE, next)});
            }
            //获取需要新增的属性并添加到条目中的属性
            for (TreeVo treeVo : attributeList) {
                Attribute newAttr = new BasicAttribute(treeVo.getKey(), treeVo.getValue());
                // 将属性添加到条目中
                attributes.put(newAttr);
                // 将更改提交到LDAP服务器
                ctx.modifyAttributes(rdn, DirContext.REPLACE_ATTRIBUTE, attributes);
            }
            ctx.close();
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SystemException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
        }
        return StaticValue.TRUE;
    }

    /**
     * 编辑属性
     *
     * @param ldapTemplate 查询模板
     * @param bindTree     参数
     * @param filter       过滤
     * @return true 成功 false 失败
     */
    public static boolean reBIndLdapTree(LdapTemplate ldapTemplate, ReBindTreDto bindTree, String filter) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        try {
            NamingEnumeration<SearchResult> results = ctx.search(bindTree.getRdn(), filter, searchControls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                // 获取节点的DN
                String oldDN = searchResult.getNameInNamespace();
                String name = searchResult.getName();
                String newDn = bindTree.getBaseDn();
                if (!ObjectUtils.isEmpty(name)) {
                    newDn = name + StaticValue.SPLIT + bindTree.getBaseDn();
                }
                // 修改节点的RDN
                ctx.rename(oldDN, newDn);
            }
            ctx.close();
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SystemException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
        }
        return StaticValue.TRUE;

    }

    /**
     * 查询字节点
     *
     * @param rdn    baseDN
     * @param filter 过滤条件
     * @param ctx    查询的模板
     * @return 返回子节点的数据
     */
    public static boolean queryChildRdn(String rdn, String filter, LdapContext ctx) {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        try {
            NamingEnumeration<SearchResult> results = ctx.search(rdn, filter, controls);
            if (results.hasMore()) {
                SearchResult result = results.next();
                String dn = result.getNameInNamespace();
                controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                results = ctx.search(dn, filter, controls);
                while (results.hasMore()) {
                    SearchResult childResult = results.next();
                    String childDn = childResult.getNameInNamespace();
                    // 删除子对象
                    delChildRdn(childDn, filter, ctx, controls);
//                    ctx.destroySubcontext(childDn);
                }
                ctx.destroySubcontext(dn);
            }
            ctx.destroySubcontext(rdn);
            ctx.close();
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SystemException(ExceptionEnum.LDAP_DEL_RDN_NOT_EXIT);
        }
        return StaticValue.TRUE;
    }

    /**
     * @param childRdn 节点RDN
     * @param filter   过滤条件
     * @param ctx      查询
     * @param controls 范围
     * @throws NamingException 异常
     */
    public static void delChildRdn(String childRdn, String filter, LdapContext ctx, SearchControls controls) throws NamingException {
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(childRdn, filter, controls);
        while (results.hasMore()) {
            SearchResult childResult = results.next();
            String childDn = childResult.getNameInNamespace();
            delChildRdn(childDn, filter, ctx, controls);
        }
        ctx.destroySubcontext(childRdn);
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
