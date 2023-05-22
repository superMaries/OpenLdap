package cn.ldap.ldap.controller;

import cn.hutool.log.Log;
import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.vo.IndexVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexService;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * 首页
 *
 * @title: IndexController
 * @Author Wy
 * @Date: 2023/3/31 11:18
 * @Version 1.0
 */
@Data
@RestController
@RequestMapping("/index/")
@Slf4j
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 获取信息信息
     *
     * @return
     */
    @PostMapping("device/status/list")
    public ResultVo<DeviceStatusRespVo> listDeviceStatus() {
        return indexService.listDeviceStatus();
    }

    /**
     * 获取网络吞吐量
     *
     * @return
     */
    @PostMapping("net/speed/list")
    public ResultVo<EvictingQueue<NetSpeedRespVo>> getNetSpeed() {
        return indexService.getNetSpeed();
    }

    /**
     * 查询总量接口
     * 查询证书接口
     * 查询CRL接口
     */
    @PostMapping("ldap/info")
    public ResultVo<IndexVo> ldapInfo(@RequestBody CertTreeDto tree) {
        return indexService.ldapInfo(tree);
    }

    /**
     * 返回ldap 总数接口
     *
     * @return 返回ldap 总数接口
     */
    @PostMapping("ldap/num")
    public ResultVo<Long> ldapTotal(@RequestBody CertTreeDto tree) {
        return indexService.ldapTotal(tree);
    }

    /**
     * @return 返回Crl数量
     */
    @PostMapping("ldap/crlNum")
    public ResultVo<Long> ldapCrlNum(@RequestBody CertTreeDto tree) {
        return indexService.ldapCrlNum(tree);
    }

    /**
     * 查询证书接口
     *
     * @return 查询证书接口
     */
    @PostMapping("ldap/certNum")
    public ResultVo<Long> ldapCertNum(@RequestBody CertTreeDto tree) {
        log.info(tree.toString());
        return indexService.ldapCertNum(tree);
    }

//    @PostMapping("testTotal")
//    public Object testTotal(){
//        String ldapUrl = "ldap://123.57.204.10:389"; // LDAP服务的URL
//        String ldapUserDn = "cn=Directory Manager,c=cn"; // LDAP管理员的DN
//        String ldapPassword = "Js3qCaLdapAdmin"; // LDAP管理员的密码
//        String baseDn = "c=cn"; // 要查询数据的基础DN
//        String filter = "(serialNumber=*)"; // 查询过滤器
//
//        try {
//            // 创建连接
//            String ldapContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
//            String ldapSecurityAuthentication = "simple";
//            String ldapReferral = "follow";
//            String ldapBindDn = ldapUserDn;
//            String ldapBindPassword = ldapPassword;
//            String ldapVersion = "3";
//            String ldapTimeout = "5000";
//
//            Hashtable<String, String> env = new Hashtable<String, String>();
//            env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContextFactory);
//            env.put(Context.PROVIDER_URL, ldapUrl);
//            env.put(Context.SECURITY_AUTHENTICATION, ldapSecurityAuthentication);
//            env.put(Context.REFERRAL, ldapReferral);
//            env.put(Context.SECURITY_PRINCIPAL, ldapBindDn);
//            env.put(Context.SECURITY_CREDENTIALS, ldapBindPassword);
//            env.put("java.naming.ldap.version", ldapVersion);
//            env.put("com.sun.jndi.ldap.connect.timeout", ldapTimeout);
//
//            DirContext ctx = new InitialDirContext(env);
//
//            // 查询数据
//            SearchControls searchControls = new SearchControls();
//            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//            searchControls.setReturningAttributes(new String[] { "serialNumber" });
//            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, searchControls);
//
//            // 统计RDN总数
//            int count = 0;
//            while (results.hasMore()) {
//                SearchResult result = results.next();
//                count++;
//            }
//
//            // 关闭连接
//            ctx.close();
//
//
//            System.out.println("Total count: " + count);
//
//            return count;
//        } catch (NamingException e) {
//            System.out.println("Search failed.");
//            e.printStackTrace();
//        }
//
//        return null;
//    }
    }





