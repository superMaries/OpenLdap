package cn.ldap.ldap.service.impl;

import cn.hutool.core.lang.copier.SrcToDestCopier;
import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.NetWorkUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.IndexVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;
import org.springframework.ldap.core.LdapTemplate;


import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @title: IndexServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 13:30
 * @Version 1.0
 */
@Service
@Slf4j
public class IndexServiceImpl implements IndexService {

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * 获取设备状态信息
     *
     * @return
     */
    @Override
    public ResultVo listDeviceStatus() {
        log.info("获取设备状态信息:");
        float cpuInfo = NetWorkUtil.getCpuInfo();
        log.info("获取cpu信息:" + cpuInfo);
        int diskInfo = 0;
        int memInfo = 0;
        try {
            memInfo = NetWorkUtil.getMemInfo();
            log.info("获取内存信息:" + memInfo);
        } catch (Exception e) {
            memInfo = 0;
            log.error("获取内存信息:" + 0);
        }
        float diskInfos;
        try {
            diskInfos = NetWorkUtil.getDiskInfo();
            log.info("获取硬盘信息:" + diskInfos);
        } catch (IOException e) {
            log.error("获取硬盘信息:" + e.getMessage());
            diskInfos = 0f;
        } catch (InterruptedException e) {
            log.error("获取硬盘信息:" + e.getMessage());
            diskInfos = 0f;
        }
        DeviceStatusRespVo deviceStatusRespVo = new DeviceStatusRespVo();
        deviceStatusRespVo.setCpuRate(cpuInfo);
        deviceStatusRespVo.setMemoryRate(memInfo);
        deviceStatusRespVo.setServerStatus(true);
        log.info("获取设备状态信息:" + deviceStatusRespVo);
        return ResultUtil.success(deviceStatusRespVo);
    }

    /**
     * 获取网络吞吐量
     *
     * @return
     */

    EvictingQueue<NetSpeedRespVo> queue = EvictingQueue.create(10);

    @Override
    public ResultVo getNetSpeed() {
        log.info("获取网络吞吐量");
        Map<String, String> netWorkDownUp = NetWorkUtil.getNetWorkDownUp();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (queue.size() == 0) {
            for (int j = 10; j > 0; j--) {
                NetSpeedRespVo netSpeedRespVo;
                if (j == 1) {
                    netSpeedRespVo = new NetSpeedRespVo();
                    netSpeedRespVo.setDateTime(now.format(dateTimeFormatter));
                    netSpeedRespVo.setDownSpeed(netWorkDownUp.get("rxPercent"));
                    netSpeedRespVo.setUpSpeed(netWorkDownUp.get("txPercent"));
                } else {
                    netSpeedRespVo = new NetSpeedRespVo();
                    String format = now.minusSeconds((j - 1) * 5).format(dateTimeFormatter);
                    netSpeedRespVo.setDateTime(format);
                    netSpeedRespVo.setDownSpeed("0");
                    netSpeedRespVo.setUpSpeed("0");
                }
                queue.add(netSpeedRespVo);
            }
        }
        NetSpeedRespVo netSpeedRespVo = new NetSpeedRespVo();
        netSpeedRespVo.setDateTime(now.format(dateTimeFormatter));
        netSpeedRespVo.setDownSpeed(netWorkDownUp.get("rxPercent"));
        netSpeedRespVo.setUpSpeed(netWorkDownUp.get("txPercent"));
        queue.add(netSpeedRespVo);
        log.info("获取网络吞吐量：" + netSpeedRespVo);
        return ResultUtil.success(queue);
    }

    /**
     * 查询总量接口
     * 查询证书接口
     * 查询CRL接口
     */
    @Override
    public ResultVo ldapInfo() {
        IndexVo indexVo = new IndexVo();
        Field[] fields = IndexVo.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName());
            CompletableFuture.supplyAsync(() -> {
                return queryFieldNum(field, indexVo);
            });
        }
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            ResultUtil.fail();
        }
        return ResultUtil.success(indexVo);
    }

    private long queryFieldNum(Field field, IndexVo indexVo) {
        switch (field.getName()) {
            case "total":
                long queryTotal = queryTotal();
                System.out.println(queryTotal);
                indexVo.setTotal(queryTotal);
                return queryTotal;
            case "certTotal":
                long queryCertTotal = queryCertTotal();
                System.out.println(queryCertTotal);
                indexVo.setCertTotal(queryCertTotal);
                return queryCertTotal;
            case "crlTotal":
                long queryCrlTotal = queryCrlTotal();
                System.out.println(queryCrlTotal);
                indexVo.setCrlTotal(queryCrlTotal);
                return queryCrlTotal;
            default:
                return 0;
        }
    }

    /**
     * 查询CRL接口
     *
     * @return
     */
    private long queryCrlTotal() {
        System.out.println("查询CRL接口");
        String base = "c=cn";
        long count = 0L;
        List<String> crlList = null;
        try {
            crlList = ldapTemplate.list(base);
        } catch (Exception e) {
            return 0;
        }
        for (String crl : crlList) {
            if (crl.startsWith("ou=crl") || crl.startsWith("ou=cacrl")) {
                base = crl + "," + base;
                count = LdapUtil.fun(ldapTemplate, base, crlList.size(), count, "ou=crl", "ou=cacrl");
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * 查询CERT接口
     *
     * @return
     */

    private long queryCertTotal() {
        System.out.println("查询CERT接口");
        String base = "c=cn";
        long count = 0L;
        List<String> certList = null;
        try {
            certList = ldapTemplate.list(base);
        } catch (Exception e) {
            return 0;
        }
        for (String cert : certList) {
            if (cert.startsWith("serialNumber=")) {
                base = cert + "," + base;
                count = LdapUtil.fun(ldapTemplate, base, certList.size(), count, "serialNumber=");
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * 查询总数接口
     *
     * @return
     */
    private long queryTotal() {


        SearchControls sc = new SearchControls();
        switch ("base") {
            case "base":
                sc.setSearchScope(SearchControls.OBJECT_SCOPE);
                break;
            case "one":
                sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                break;
            default:
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                break;
        }
        NamingEnumeration ne = null;
        ldapTemplate.lookupContext("");

        DirContext dc = null;

        dc = ldapTemplate.lookupContext("dc=super,dc=com");
        Name dn = ((DirContextAdapter) dc).getDn();
        Long count = 0L;
        List<String> rootList = ldapTemplate.list(dn);
        count += rootList.stream().count();
        for (String root : rootList) {
            List<String> list = ldapTemplate.list(root + ",dc=super,dc=com");
            count += list.stream().count();
        }


        return count + 1;
    }


}




