package cn.ldap.ldap.common.util;

import cn.ldap.ldap.common.dto.*;
import cn.ldap.ldap.common.enums.CertificateEnum;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.ImportEnum;
import cn.ldap.ldap.common.enums.LdapAccuntAuthEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.LdapAccountVo;
import cn.ldap.ldap.common.vo.TreeVo;
import isc.authclt.IscJcrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @title: LdapUtil
 * @Author Wy
 * @Date: 2023/4/4 9:16
 * @Version 1.0
 */
@Slf4j
public class LdapUtil {


    public static List<LdapAccountVo> queryLdapAccount(LdapTemplate newLdapTemplate, String filter, String baseDn) {
        List<LdapAccountVo> ldapList = new ArrayList<>();
        LdapContext ctx = (LdapContext) newLdapTemplate.getContextSource().getReadOnlyContext();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] attrIDs = {"uid", "userPassword"};
        controls.setReturningAttributes(attrIDs);

        try {
            NamingEnumeration<SearchResult> answer = ctx.search(baseDn, "(objectClass=person)", controls);
//            boolean hasMore = answer.hasMore();、
            log.info("查询：{}", answer.hasMore());
            while (answer.hasMore()) {
                LdapAccountVo vo = new LdapAccountVo();
                SearchResult sr = answer.next();
                log.info("信息为：{}", sr);
//                Attributes attrs = sr.getAttributes();
                String account = sr.getNameInNamespace();
//                System.out.println("Username: " + attrs.get("uid").get());
//                System.out.println("Password: " + new String((byte[]) attrs.get("userPassword").get()));
                vo.setAccount(account);
//                vo.setAccount(attrs.get("uid").toString());
//                vo.setPwd(new String((byte[]) attrs.get("userPassword").get()));
//                answer.hasMore();
                ldapList.add(vo);
            }
        } catch (NamingException e) {
            log.error("查询账户异常：{}", e.getMessage());
        }
        return ldapList;
    }


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
    public static Long queryLdapNum(LdapTemplate ldapTemplate, String ldapSearchFilter, String ldapSearchBase, String... whereParam) {
        LdapQuery filter = query().filter(ldapSearchFilter);
        AndFilter andFilter = new AndFilter();
        andFilter.and(filter.filter());
        if (!ObjectUtils.isEmpty(whereParam)) {
            for (int i = 0; i < whereParam.length; i++) {
                LdapQuery orF = query().filter(whereParam[i]);
                OrFilter orFilter = new OrFilter();
                orFilter.or(orF.filter());
                andFilter.and(orFilter);

            }
        }
        AttributesMapper mapper = new AttributesMapper() {
            @Override
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                return attributes;
            }
        };
        try {
            log.info("开始查询");
            String encode = andFilter.encode();
            //   String encode = "(&(objectClass=*)(serialNumber=*))";
            log.info("查询过滤:{}", encode);

            long size = ldapTemplate.search(ldapSearchBase, encode, mapper).size();
            log.info("结束查询，查询结果为:{}", size);
            return size;
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e.getMessage().contains(StaticValue.NOT_CONNECT)) {
                throw new SysException(ExceptionEnum.LDAP_CONNECT_ERROR);
            }
            throw new SysException(ExceptionEnum.LDAP_ERROR);
        }
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
     * 查询节点属性详情
     *
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
        // 解析 dn 的值
        String parentStr = baseDN.split(StaticValue.SPLIT)[0];
        //dn 的值不可修改，于是将dn 的值进行解析
        String[] addStr = parentStr.split(StaticValue.ADD);
        List<String> list = Arrays.asList(addStr);

        try {
            //查询对应的属性值 对返回属性的值进行解析
            if (!ObjectUtils.isEmpty(attribute)) {
                attributes = ctx.getAttributes(baseDN, attribute.split(StaticValue.SPLIT));
            } else {
                attributes = ctx.getAttributes(baseDN);
            }
            NamingEnumeration<? extends Attribute> attributesAll = attributes.getAll();
            //解析属性值
            while (attributesAll.hasMore()) {
                Attribute next = attributesAll.next();
                String key = next.getID();
                if (isRetrunAttr) {
                    TreeVo treeVo = new TreeVo();
                    treeVo.setKey(key);
                    treeVos.add(treeVo);
                    continue;
                }
                NamingEnumeration<?> keyAll = next.getAll();
                while (keyAll.hasMore()) {
                    TreeVo treeVo = new TreeVo();
                    Object o = keyAll.nextElement();
                    String attrValue = o.toString();
                    if (o instanceof byte[]) {
                        byte[] cert = (byte[]) o;
                        attrValue = Base64.getEncoder().encodeToString(cert);
                    }
                    String queryRn = key + StaticValue.EQ + attrValue;
                    //获取dn 不可修改的值
                    List<String> collect = list.stream().filter(it -> it.equals(queryRn)).collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(collect) && collect.size() >= StaticValue.COUNT) {
                        treeVo.setFlag(StaticValue.TRUE);
                    } else if (StaticValue.OBJECT_CLASS.toUpperCase().equals(key.toUpperCase().trim())) {
                        treeVo.setFlag(StaticValue.TRUE);
                    }

                    if (StaticValue.USER_CERTIFICATE.toLowerCase().equals(key.toLowerCase())) {
                        //证书文件
                        String cert = IscSignUtil.otherToBase64(attrValue);
                        IscJcrypt iscJcrypt = new IscJcrypt();
                        String certInfo = iscJcrypt.getCertInfo(cert, CertificateEnum.CERT_NAME.getCode());
                        treeVo.setTitle(certInfo);
                    } else {
                        treeVo.setTitle(attrValue);
                    }
                    treeVo.setValue(attrValue);
                    treeVo.setKey(key);
                    treeVos.add(treeVo);
                }
            }
        } catch (NamingException e) {
            log.error("ldap 查询属性异常:{}", e.getMessage());
            return treeVos;
        }
        return treeVos;
    }


    public static List<TreeVo> queryAttributeBytesInfo(LdapTemplate ldapTemplate, String baseDN, boolean isRetrunAttr, String attribute) {
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
                    Object o = keyAll.nextElement();
                    String attrValue = o.toString();
                    if (o instanceof byte[]) {
                        byte[] cert = (byte[]) o;
                        attrValue = Base64.getEncoder().encodeToString(cert);

                    }

                    String finalAttrValue = attrValue;
                    List<String> collect = list.stream().filter(it -> it.equals(key + StaticValue.EQ + finalAttrValue)).collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(collect.size()) && collect.size() >= StaticValue.COUNT) {
                        treeVo.setFlag(StaticValue.TRUE);
                    } else if (StaticValue.OBJECT_CLASS.toUpperCase().equals(key.toUpperCase().trim())) {
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


    public static List<CertTreeVo> queryCertTree(LdapTemplate ldapTemplate, String ldapSearchFilter, String ldapSearchBase, Integer scope, Integer pageSize, Integer page, Map<String, Object> map) {

        List<CertTreeVo> certTreeVos = new ArrayList<>();

        //DirContext ctx = null;
        //ctx = ldapTemplate.getContextSource().getReadOnlyContext();
        // LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
        // LdapContext ctx =null;
        // ctx= (LdapContext)contextSource.getReadWriteContext();
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(scope);
        //查询多少条

        if (map != null) {
//            AttributesMapper mapper = new AttributesMapper() {
//                @Override
//                public Object mapFromAttributes(Attributes attributes) throws NamingException {
//                    return attributes;
//                }
//            };
//            long total = 0l;
//            try {
//                total = ldapTemplate.search(ldapSearchBase, ldapSearchFilter, mapper).size();
//            } catch (Exception e) {
//                log.error(e.getMessage());
//                total = 0l;
//            }
//            map.put("total", total);
//            map.put("page", total / pageSize + (total % pageSize != 0 ? 1 : 0));
            map.put("data", certTreeVos);
        }

//        -------------------------
        //page 默认为1 pageSize 1000
        //      总共需要查的数据
        long endNum = page * pageSize;
        //开始的数量
        long startNum = (page - 1) * pageSize;

        long count = 0;
        //        searchControls.setCountLimit(totalNodeCount);
        try {
            //设置每页查询的数量
//            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE,
//                    Control.CRITICAL)};
            Control[] controls = new Control[]{new PagedResultsControl(page * pageSize, Control.CRITICAL)};
            ctx.setRequestControls(controls);

            byte[] cookie = null;

            do {
                //分页查询
                NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter, searchControls);
                //统计总数
                log.info("证书目录数：{}", results.hasMore());
                while (results.hasMore()) {
                    SearchResult result = results.next();
                    log.info("证书目录值：{}", result);
                    if (count < startNum) {
                        count++;
                        continue;
                    }
                    if (count == endNum) {
                        cookie = null;
                        break;
                    }
                    count++;


                    CertTreeVo certTreeVo = new CertTreeVo();

                    String baseDn = result.getName();
                    if (ObjectUtils.isEmpty(baseDn)) {
                        String[] split = ldapSearchBase.split(StaticValue.SPLIT);
                        baseDn = split[0];
                    }
                    certTreeVo.setBaseDn(baseDn);

                    String newName = baseDn.split(StaticValue.SPLIT)[StaticValue.SPLIT_COUNT];
                    try {
                        String fullName = result.getNameInNamespace();
                        String parentRdn = fullName.replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
                        //设置父级的RDN
                        certTreeVo.setParentRdn(parentRdn);
                        certTreeVo.setRdn(fullName);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        certTreeVo.setRdn(certTreeVo.getBaseDn());
                        String parentRdn = certTreeVo.getBaseDn().replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
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

//    public static List<CertTreeVo> queryCertTree(LdapTemplate ldapTemplate, String ldapSearchFilter,
//                                                 String ldapSearchBase, Integer scope,
//                                                 Integer pageSize, Integer page,
//                                                 Map<String, Object> map) {
//
//        List<CertTreeVo> certTreeVos = new ArrayList<>();
//        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
//        SearchControls searchControls = new SearchControls();
//        searchControls.setSearchScope(scope);
//        //查询多少条
//
//        if (map != null) {
//            AttributesMapper mapper = new AttributesMapper() {
//                @Override
//                public Object mapFromAttributes(Attributes attributes) throws NamingException {
//                    return attributes;
//                }
//            };
//            long total = ldapTemplate.search(ldapSearchBase, ldapSearchFilter, mapper).size();
//            map.put("total", total);
//            map.put("page", total / pageSize + (total % pageSize != 0 ? 1 : 0));
//            map.put("data", certTreeVos);
//        }
//        //page 默认为1 pageSize 1000
//        //      总共需要查的数据
//        long endNum = page * pageSize;
//        //开始的数量
//        long startNum = (page - 1) * pageSize;
//
//        long count = 0;
//        //        searchControls.setCountLimit(totalNodeCount);
//        try {
//            //设置每页查询的数量
////            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE,
////                    Control.CRITICAL)};
//            Control[] controls = new Control[]{new PagedResultsControl(page * pageSize,
//                    Control.CRITICAL)};
//            ctx.setRequestControls(controls);
//
//            byte[] cookie = null;
//
//            do {
//                //分页查询
//                NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter,
//                        searchControls);
//                //统计总数
//                while (results.hasMore()) {
//                    SearchResult result = results.next();
//                    if (count < startNum) {
//                        count++;
//                        continue;
//                    }
//                    if (count == endNum) {
//                        cookie = null;
//                        break;
//                    }
//                    count++;
//
//
//                    CertTreeVo certTreeVo = new CertTreeVo();
//
//                    String baseDn = result.getName();
//                    if (ObjectUtils.isEmpty(baseDn)) {
//                        String[] split = ldapSearchBase.split(StaticValue.SPLIT);
//                        baseDn = split[0];
//                    }
//                    certTreeVo.setBaseDn(baseDn);
//
//                    String newName = baseDn.split(StaticValue.SPLIT)[StaticValue.SPLIT_COUNT];
//                    try {
//                        String fullName = result.getNameInNamespace();
//                        String parentRdn = fullName.replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
//                        //设置父级的RDN
//                        certTreeVo.setParentRdn(parentRdn);
//                        certTreeVo.setRdn(fullName);
//                    } catch (Exception e) {
//                        log.error(e.getMessage());
//                        certTreeVo.setRdn(certTreeVo.getBaseDn());
//                        String parentRdn = certTreeVo.getBaseDn().replace(newName + StaticValue.SPLIT, StaticValue.REPLACE);
//                        //设置父级的RDN
//                        certTreeVo.setParentRdn(parentRdn);
//                    }
//                    certTreeVos.add(certTreeVo);
//
//
//                }
//                //获取最近一次 LDAP 操作的响应控制器。
//                Control[] responseControls = ctx.getResponseControls();
//                //设置Cookies
//                if (responseControls != null) {
//                    for (Control control : responseControls) {
//                        if (control instanceof PagedResultsResponseControl) {
//                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
//                            cookie = prrc.getCookie();
//                        }
//                    }
//                }
//                ctx.setRequestControls(new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, cookie, Control.CRITICAL)});
//            } while (cookie != null);
//            ctx.close();
//        } catch (NamingException | IOException e) {
//            log.error(e.getMessage());
//            return certTreeVos;
//        }
//        return certTreeVos;
//    }

    /**
     * @param ldapTemplate 查询模板
     * @param scope        查询范围
     * @param baseDN       查询条件
     * @param filter       过滤条件
     * @return
     */
    public static Map<String, Object> queryTreeRdnOrNumEx(Map<String, Object> map, LdapTemplate ldapTemplate, Integer scope, String baseDN, String filter) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(scope);

        int pageSize = StaticValue.LDAP_PAGE_SIZE;
        map.put(StaticValue.RDN, baseDN);
        AttributesMapper mapper = new AttributesMapper() {
            @Override
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                return attributes;
            }
        };

        long total = ldapTemplate.search(baseDN, filter, SearchControls.SUBTREE_SCOPE, mapper).size();
        long totalNodeCount = ldapTemplate.search(baseDN, filter, SearchControls.ONELEVEL_SCOPE, mapper).size();


        map.put(StaticValue.RDN_NUM_KEY, totalNodeCount);
        map.put(StaticValue.RDN_CHILD_NUM_KEY, total);
        return map;
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
            throw new SysException(ExceptionEnum.QUERY_POINT_ERROR);
        } finally {
            // 关闭LDAP连接
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                log.info("节点 {} 不存在", e.getMessage());
                throw new SysException(ExceptionEnum.QUERY_POINT_ERROR);
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
            //需要修改属性的值
            List<TreeVo> attributeList = ldapBindTreeDto.getAttributes();
            List<String> oldAtt = new ArrayList<>();
            while (attributesAll.hasMore()) {
                Attribute next = attributesAll.next();
                String key = next.getID();
                //查询到key对应的values 的值
                List<TreeVo> keys = attributeList.stream().filter(it -> it.getKey().equals(key)).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(keys)) {
                    continue;
                }
                next.clear();
                for (TreeVo value : keys) {
                    //判断是否是证书
                    if (StaticValue.USER_CERTIFICATE.toLowerCase().equals(key.toLowerCase())) {
                        String cert = IscSignUtil.otherToBase64(value.getValue());
                        byte[] certByte = decodeCertificate(cert);
                        next.add(certByte);
                    } else {
                        next.add(value.getValue());
                    }
                    //移除已经修改的数据
//                    TreeVo vo = new TreeVo();
//                    vo.setValue(value.getValue());
//                    vo.setFlag(vo.isFlag());
//                    vo.setTitle(vo.getTitle());
//                    vo.setKey(key);
                    attributeList.remove(value);
                }
                //获取以及添加的key
                oldAtt.add(key);
                ctx.modifyAttributes(rdn, new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE, next)});
            }
            //获取需要新增的属性并添加到条目中的属性
            for (TreeVo treeVo : attributeList) {
                Object value = treeVo.getValue();
                if (treeVo.getKey().toLowerCase().equals(StaticValue.USER_CERTIFICATE.toLowerCase())) {
                    String cert = IscSignUtil.otherToBase64(treeVo.getValue());
                    byte[] certByte = decodeCertificate(cert);
                    value = certByte;
                }
                Attribute newAttr = new BasicAttribute(treeVo.getKey(), value);
                // 将属性添加到条目中
                attributes.put(newAttr);
                // 将更改提交到LDAP服务器
                ctx.modifyAttributes(rdn, DirContext.REPLACE_ATTRIBUTE, attributes);
            }
            ctx.close();
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
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
            throw new SysException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
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
            while (results.hasMore()) {
                SearchResult result = results.next();
                String dn = result.getNameInNamespace();
                controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                NamingEnumeration<SearchResult> chldResults = ctx.search(dn, filter, controls);
                while (chldResults.hasMore()) {
                    SearchResult childResult = chldResults.next();
                    String childDn = childResult.getNameInNamespace();
                    // 删除子对象
                    delChildRdn(childDn, filter, ctx, controls);
//                    ctx.destroySubcontext(childDn);
                }
                ctx.destroySubcontext(dn);
            }
            ctx.destroySubcontext(rdn);
//
//            Control[] controlsEx = new Control[]{new PagedResultsControl(0, true)};
//            ctx.setRequestControls(controlsEx);
            Control[] controlsEx = ctx.getConnectControls();
            ctx.reconnect(controlsEx);


            ctx.close();
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_DEL_RDN_NOT_EXIT);
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

    /**
     * 导出文件（备份）
     *
     * @param ldapTemplate 查询模板
     * @param exportDto    参数
     * @param response     返回
     * @return true 成功 false 失败
     */
    public static Boolean exportLdifFile(LdapTemplate ldapTemplate, LdifDto exportDto, HttpServletResponse response) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();

        Integer scope = exportDto.getScope();
        //设置导出的条件  范围 0 当前 1 一个条目 2 全部
        Integer queryScope = (StaticValue.LDAP_SCOPE.equals(exportDto.getScope())) ? SearchControls.OBJECT_SCOPE : scope;
        searchControls.setSearchScope(queryScope);

        int pageSize = StaticValue.LDAP_PAGE_SIZE;
        searchControls.setCountLimit(pageSize);

        //设置过滤值和查询值
        String ldapSearchBase = exportDto.getBaseDN();
        String ldapSearchFilter = exportDto.getBaseFilter();

        //导出文件位置
        String fileName = exportDto.getExportFilePath();
        if (StaticValue.EXPORT_LOCAL.equals(exportDto.getExportType())) {
            //下载到本地
            fileName = TimeUtil.getNowTimeStr() + StaticValue.LDIF;
        } else {
            if (ObjectUtils.isEmpty(fileName)) {
                log.error("缺少文件保存地址参数");
                throw new SysException(ExceptionEnum.PARAM_ERROR);
            }
            if (!fileName.endsWith(StaticValue.LDIF)) {
                fileName += StaticValue.LDIF;
            }
        }

        try {
            //设置每页查询的数量
            Control[] controls = new Control[]{new PagedResultsControl(StaticValue.LDAP_PAGE_SIZE, Control.CRITICAL)};
            ctx.setRequestControls(controls);
            // 创建LDIF文件输出流
            try (PrintWriter ldifWriter = new PrintWriter(new FileWriter(fileName));) {
                byte[] cookie = null;
                do {
                    //分页查询
                    NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, ldapSearchFilter, searchControls);
                    //统计总数
                    while (results.hasMore()) {
                        SearchResult result = results.nextElement();
                        if (ObjectUtils.isEmpty(result)) {
                            continue;
                        }
                        //设置RDN的值
                        String rdn = result.getNameInNamespace();
                        ldifWriter.println("dn" + ": " + rdn);
                        if (exportDto.isOnlyRdn()) {
                            continue;
                        }
                        Attributes attributes = result.getAttributes();
                        NamingEnumeration<? extends Attribute> attributesAll = attributes.getAll();
                        //解析属性值
                        while (attributesAll.hasMore()) {
                            Attribute next = attributesAll.next();
                            String key = next.getID();
                            NamingEnumeration<?> keyAll = next.getAll();
                            while (keyAll.hasMore()) {
                                Object o = keyAll.nextElement();
                                String attrValue = o.toString();
                                //判断是否字节格式
                                if (o instanceof byte[]) {
                                    byte[] cert = (byte[]) o;
                                    attrValue = Base64.getEncoder().encodeToString(cert);
                                }
                                ldifWriter.println(key + ": " + attrValue);
                            }
                        }
                        ldifWriter.println(StaticValue.N);
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
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.READ_FILE_ERROR);
            }
            ctx.close();
            return downLdif(exportDto, response, fileName);

        } catch (NamingException | IOException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
        }
    }

    public static boolean downLdif(LdifDto exportDto, HttpServletResponse response, String fileName) {
        //判断是下载到服务器还是本地
        if (StaticValue.EXPORT_LOCAL.equals(exportDto.getExportType())) {
            Path file = Paths.get(fileName);
            try {
                response.setContentType("application/ldif");
                response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
                return true;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.FILE_IO_ERROR);
            }
        } else {
            Path file = Paths.get(fileName);
            //下载到服务器。判断文件是否存在 不存在就创建 不需要返回下的文件
            if (!Files.exists(file)) {
                //不存在
                try {
                    Files.createFile(file);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new SysException(ExceptionEnum.FILE_IO_ERROR);
                }
            }
            return true;
        }
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

    /**
     * 根据LDAP文件进行 新增
     *
     * @param ldapTemplate LDAP模板
     * @param file         上传的文件
     * @param name         文件名称
     * @param type         1 仅添加  2 仅更新 3 更新或添加
     * @return
     */
    public static boolean importLap(LdapTemplate ldapTemplate, MultipartFile file, String name, Integer type) {
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
//            byte[] bytes = file.getBytes();
            String line;
            inputStream = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
//            String code=StaticValue.UTF_8;
//            try {
//                code = FileCodeUtil.codeStr(inputStream);
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                throw new SysException(ExceptionEnum.FILE_IO_ERROR);
//            }
            Integer count = StaticValue.SPLIT_COUNT;

            StringBuilder sb = new StringBuilder();
            boolean result = StaticValue.FALSE;
            line = reader.readLine();
            while (line != null) {
                if (line.contains(StaticValue.DNK) || line.contains(StaticValue.DNM)) {
                    count++;
                }
                //统计的数据 100 然后可以开始解析
                if (count == StaticValue.HUNDRED) {
                    result = StaticValue.TRUE;
                    count = StaticValue.SPLIT_COUNT;
                    // 解析 LDIF 文件
                    analysisLdapFile(ctx, sb, type);
                    sb = new StringBuilder();
                }

                boolean isLine = StaticValue.FALSE;
                sb.append(line);
                if (line.contains(StaticValue.BINARY)) {
                    //证书 、CRL 需要将换行的数据换在一行
                    while (StaticValue.TRUE) {
                        isLine = StaticValue.TRUE;
                        line = reader.readLine();
                        if (ObjectUtils.isEmpty(line) || line.contains(StaticValue.BINARY) || StaticValue.REPLACE.equals(line)) {
//                            sb.delete(sb.length() - line.length(), sb.length());
                            break;
                        }
                        sb.append(line);
                    }
                }
                sb.append(StaticValue.N);
                result = StaticValue.FALSE;
                if (!isLine) line = reader.readLine();
            }
            if (!result) {
                analysisLdapFile(ctx, sb, type);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.FILE_IO_ERROR);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

        return StaticValue.TRUE;
    }

    /**
     * 解析 LDIF 文件
     *
     * @param sb   LDIF 文件里面的内容
     * @param type 1 仅添加  2 仅更新 3 更新或添加
     */
    private static void analysisLdapFile(LdapContext ctx, StringBuilder sb, Integer type) {
        String[] entries = sb.toString().split(StaticValue.N + StaticValue.N);
        for (String entry : entries) {

            // 解析 LDIF 条目的属性
            Attributes attributes = new BasicAttributes();
            Integer count = parseAttributes(entry, attributes);
            if (count == StaticValue.SPLIT_COUNT) {
                continue;
            }
            try {
                //获取DN的值
                String dn = attributes.get(StaticValue.DN).get().toString();
                // 判断条目是否已存在
                Attributes dnAttr = null;
                try {
                    dnAttr = ctx.getAttributes(dn);
                } catch (NameNotFoundException e) {
                    dnAttr = null;
                }
                if (dnAttr == null) {
                    //  判断导入的类型  如果传入的类型是仅更新 则忽略
                    if (!ImportEnum.ONLY_UPDATE.getCode().equals(type)) {
                        // 创建新条目
                        attributes.remove(StaticValue.DN);
                        ctx.createSubcontext(dn, attributes);
                    }
                } else {
                    //  判断导入的类型  如果传入的类型是仅添加 则忽略
                    if (!ImportEnum.ONLY_INTER.getCode().equals(type)) {
                        // 更新已有条目
//                        因为里面有一个值为DN，这个是唯一标识 不能放在属性里面，于是需要移除DN
                        attributes.remove(StaticValue.DN);
//                        ModificationItem[] mods = new ModificationItem[attributes.size()];
                        ModificationItem[] mods = new ModificationItem[count - StaticValue.COUNT];
                        NamingEnumeration<? extends Attribute> attrs = attributes.getAll();
                        int i = 0;
                        while (attrs.hasMore()) {
                            //获取属性
                            Attribute attr = attrs.next();
                            if (StaticValue.DN.toUpperCase().equals(attr.getID().toUpperCase())) {
                                continue;
                            }
                            String id = attr.getID();
//                            Object o = attr.get();
                            NamingEnumeration<?> all = attr.getAll();
                            while (all.hasMore()) {
                                Object o = all.nextElement();
                                String attrValue = o.toString();
                                mods[i++] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(id, o));
                            }
                        }
                        //更新
//                        ctx.modifyAttributes(dn, mods);
                        ctx.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, attributes);
                    }
                }
            } catch (NamingException e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
            }
        }
    }

    /**
     * 证书转字节
     *
     * @param certificate 证书字符串
     * @return 字节
     */
    public static byte[] decodeCertificate(String certificate) {
        return Base64.getDecoder().decode(certificate);
    }

    /**
     * 解析 LDIF 条目的属性
     *
     * @param entry
     */
    private static Integer parseAttributes(String entry, Attributes attributes) {
        Integer count = StaticValue.SPLIT_COUNT;
        //对数据进行分割
        String[] lines = entry.split(StaticValue.N);
        //解析
        for (String line : lines) {
            if (ObjectUtils.isEmpty(line)) {
//                if (line.startsWith(StaticValue.J)) {
//                    // 注释行
//                    continue;
//                }
                continue;
            }
            line = line.replace(StaticValue.mh, StaticValue.m);
            String[] parts = line.split(StaticValue.m, StaticValue.index);
            // TODO: 2023/4/17 判断长度
            //获取key 和 value 的值
            if (parts.length < 2) {
                System.out.println(line);
            }
            String name = parts[StaticValue.SPLIT_COUNT];
            Object value = parts[StaticValue.COUNT].startsWith(StaticValue.m) ? parts[StaticValue.COUNT].substring(StaticValue.ONE) : parts[StaticValue.COUNT];

            //判断是否是证书
//            if (name.toUpperCase().equals(StaticValue.USER_CERTIFICATE.toUpperCase())) {
            if (name.toUpperCase().contains(StaticValue._BINARY.toUpperCase())) {
                String certStr = IscSignUtil.otherToBase64(parts[StaticValue.COUNT]);
                value = decodeCertificate(certStr);
            }

            Attribute attribute = attributes.get(name);
            if (attribute == null) {
                attribute = new BasicAttribute(name);
                attributes.put(attribute);
            }
            count++;
            attribute.add(value);
        }
        return count;

    }

    /**
     * 创建属性以及属性值
     *
     * @param createAttDtos
     * @return 返回属性
     */
    private static Attributes parseAttributes(List<CreateAttDto> createAttDtos) {
        log.info("设置属性值的参数：{}", createAttDtos);
        //创建一个 属性对象
        Attributes attributes = new BasicAttributes();
        for (CreateAttDto att : createAttDtos) {
            String name = att.getKey();
            if (StaticValue.CREATE_USER_CERTIFICATE.toLowerCase().equals(name.toLowerCase())) {
                name = StaticValue.USER_CERTIFICATE;
            } else if (StaticValue.certificateRevocationList.toLowerCase().contains(name.toLowerCase())) {
                name = StaticValue.certificateRevocationList;
            } else if (StaticValue.authorityRevocationList.toLowerCase().contains(name.toLowerCase())) {
                name = StaticValue.authorityRevocationList;
            } else if (StaticValue.deltaRevocationList.toLowerCase().contains(name.toLowerCase())) {
                name = StaticValue.deltaRevocationList;
            }
            Attribute attribute = new BasicAttribute(name);
            attributes.put(attribute);
            List<String> values = att.getValues();
            for (String value : values) {
                if (StaticValue.USER_CERTIFICATE.toLowerCase().equals(name.toLowerCase()) || StaticValue.certificateRevocationList.toLowerCase().contains(name.toLowerCase()) || StaticValue.authorityRevocationList.toLowerCase().contains(name.toLowerCase()) || StaticValue.deltaRevocationList.toLowerCase().contains(name.toLowerCase())) {
                    //证书
                    String cert = IscSignUtil.otherToBase64(value);
                    byte[] certificate = decodeCertificate(cert);
                    attribute.add(certificate);
                } else {
                    attribute.add(value);
                }
            }
        }
        return attributes;
    }

    /**
     * 根据LDAP文件进行 新增
     *
     * @param createLdapDto 参数
     * @param ldapTemplate  LDAP模板
     * @return true 成功 false 失败
     */
    public static boolean crateLdap(LdapTemplate ldapTemplate, CreateLdapDto createLdapDto) {
        //创建一个 属性对象
        Attributes attributes = new BasicAttributes();
        if (!ObjectUtils.isEmpty(createLdapDto.getCreateAttDtos())) {
            attributes = parseAttributes(createLdapDto.getCreateAttDtos());
        }
        //获取context 连接方式
        String rdn = createLdapDto.getRdn();
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        try {
            ctx.createSubcontext(rdn, attributes);
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_RDN_NOT_EXIT);
        }
        return StaticValue.TRUE;
    }

    public static boolean isExitRdn(LdapTemplate ldapTemplate, String rdn) {
        List<CertTreeVo> certTreeVos = new ArrayList<>();
        LdapContext ctx = (LdapContext) ldapTemplate.getContextSource().getReadOnlyContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        AttributesMapper mapper = new AttributesMapper() {
            @Override
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                return attributes;
            }
        };
        try {
            long total = ldapTemplate.search(rdn, StaticValue.FILTER, mapper).size();
            if (total < StaticValue.COUNT) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public static Boolean addLdapAccount(LdapTemplate newLdapTemplate, String ldapSearchFilter, String ldapSearchBase, LdapAccountDto ldapAccountDto, String filePath) throws NamingException {
        String accountName = ldapAccountDto.getAccount();
        String accountPassword = ldapAccountDto.getPwd();
        // 创建一个新的LDAP条目
        Attributes attrs = new BasicAttributes();
        Attribute oc = new BasicAttribute(StaticValue.OBJECT_CLASS);
        oc.add(StaticValue.TOP);
        oc.add(StaticValue.PERSON);
        attrs.put(oc);
        attrs.put(StaticValue.CN, ldapAccountDto.getNode());
        attrs.put(StaticValue.SN, ldapAccountDto.getNode());
        attrs.put(StaticValue.USER_PASSWORD, accountPassword);

        // 将新的LDAP条目添加到LDAP目录树中
        LdapContext ctx = (LdapContext) newLdapTemplate.getContextSource().getReadOnlyContext();
        String rdn = accountName + StaticValue.DH + ldapAccountDto.getNode();
        try {
            ctx.createSubcontext(rdn, attrs);
        } catch (NamingException e) {
            log.error(e.getMessage());
            throw new SysException(e.getMessage());
        }
        //设置权限
        return setAuthEx(rdn, ldapAccountDto.getAuth(), filePath);
    }

    public static boolean setAuthEx(String rdn, Integer authCode, String filePath) throws NamingException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        String authName = LdapAccuntAuthEnum.getTitleByCode(authCode);
        if (ObjectUtils.isEmpty(authName)) {
            authName = LdapAccuntAuthEnum.READ.getTitle();
        }
        //获取文件内容
        StringBuilder configFileData = getConfigFileData(filePath);
        String fileContent = configFileData.toString();
        String[] fileContentSplit = fileContent.split("\n");

        String data = "access to dn.subtree=" + rdn + StaticValue.N + "    by dn.base=" + "\"" + rdn + "\" write " + StaticValue.N + "    by * read";
        if (LdapAccuntAuthEnum.WRITE.getCode() != authCode) {
            //移除改字符串
            String fileContentEx = fileContent.replace(data, "");
            fileContent = fileContentEx;
            log.info("移除改字符串值：{}", fileContent);
        } else {
            StringBuilder appadStr = new StringBuilder();
            if (!fileContent.contains(data)) {
                //添加
                for (int i = 0; i < fileContentSplit.length; i++) {
                    if (fileContentSplit[i].startsWith("#关闭匿名访问")) {
                        appadStr = appadStr.append(StaticValue.N).append(fileContentSplit[i]);
                        appadStr = appadStr.append(StaticValue.N).append(data);
                    } else {
                        appadStr = appadStr.append(StaticValue.N).append(fileContentSplit[i]);
                    }
                }
            }
            fileContent = appadStr.toString();
            log.info("配置文件的值：{}", fileContent);
        }
        String fileContentEx = fileContent.replace("access to * by * none", "#access to * by * none");
        fileContent = fileContentEx;
        log.info("插入配置文件的值：{}", fileContent);
        try {
//        采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            bufferedWriter.write(fileContent);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return StaticValue.TRUE;

    }

    public static boolean setAuth(String rdn, Integer authCode, String filePath) throws NamingException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        String authName = LdapAccuntAuthEnum.getTitleByCode(authCode);
        if (ObjectUtils.isEmpty(authName)) {
            authName = LdapAccuntAuthEnum.READ.getTitle();
        }

//        String data = "access to *  by dn=" + "\"" + rdn + "\" write  by * read";
        String data = "    by dn.base=" + "\"" + rdn + "\" write ";
        //获取文件内容
        StringBuilder configFileData = getConfigFileData(filePath);
        String fileContent = configFileData.toString();
        String[] fileContentSplit = fileContent.split("\n");

        StringBuilder appadStr = new StringBuilder();

        if (LdapAccuntAuthEnum.WRITE.getCode() != authCode) {
            //移除改字符串
            String fileContentEx = fileContent.replace(data + StaticValue.N, "");
            fileContent = fileContentEx;
        } else {
            //判断是否存在 然后进行判断是否有改该行字符串
            boolean isAdd = false;
            for (int i = 0; i < fileContentSplit.length; i++) {
                if (fileContentSplit[i].startsWith("access to * by * none")) {
                    appadStr = appadStr.append(StaticValue.N).append("#" + fileContentSplit[i]);
                }
                if (fileContentSplit[i].startsWith("#关闭匿名访问")) {
                    appadStr = appadStr.append(StaticValue.N).append(fileContentSplit[i]);
                    if (fileContentSplit[i + 1].startsWith("access to *")) {
                        //为了将access to * 添加在账户前面
                        appadStr = appadStr.append(StaticValue.N).append(fileContentSplit[i + 1]);
                        int j = i + 1;
                        boolean b = false;
                        log.info("设置账户读写权限");
                        while (true) {
                            if (fileContentSplit[j + 1].startsWith(data)) {
                                b = true;
                                break;
                            }
                            if (fileContentSplit[j + 1].startsWith("database")) {
                                break;
                            }
                            j++;
                        }
                        if (!b) {
                            appadStr = appadStr.append(StaticValue.N).append(data);
                        }
                        i = i + 1;
                    } else {
                        appadStr = appadStr.append(StaticValue.N).append("access to *");
                        appadStr = appadStr.append(StaticValue.N).append(data);
                        appadStr = appadStr.append(StaticValue.N).append("    by * read");
                    }
                } else {
                    appadStr = appadStr.append(StaticValue.N).append(fileContentSplit[i]);
                }
            }
            fileContent = appadStr.toString();
//                //没有  添加
//                if (!fileContent.contains(data)) {
//                    configFileData = configFileData.append(StaticValue.N).append(data);
//                    fileContent = configFileData.toString();
//                }
        }


        try {
//        采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            bufferedWriter.write(fileContent);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        ModificationItem[] mods = new ModificationItem[1];
//        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
//                new BasicAttribute("access", "readWrite"));
//        ctx.modifyAttributes(rdn, mods);
        return StaticValue.TRUE;
    }

    /**
     * 读取文件内容
     *
     * @param filePath 文件地址
     * @return
     */
    public static StringBuilder getConfigFileData(String filePath) {
        try {
            File inputFile = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            reader.close();
            return sb;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.FILE_IO_ERROR);
        }
    }
}
