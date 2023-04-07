package cn.ldap.ldap.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.copier.SrcToDestCopier;
import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.QueryEnum;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.NetWorkUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.IndexVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;


import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

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
    /**
     * 设置过滤器
     */
    @Value("${ldap.searchFilter}")
    private String ldapSearchFilter;
    /**
     * 设置要查询的基本DN
     */
    @Value("${ldap.searchBase}")
    private String ldapSearchBase;

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * 获取设备状态信息
     *
     * @return
     */
    @Override
    public ResultVo<DeviceStatusRespVo> listDeviceStatus() {
        log.info("获取设备状态信息:");
        float cpuInfo = NetWorkUtil.getCpuInfo();
        log.info("获取cpu信息:" + cpuInfo);
        int memInfo = 0;
        try {
            memInfo = NetWorkUtil.getMemInfo();
            log.info("获取内存信息:" + memInfo);
        } catch (Exception e) {
            memInfo = 0;
            log.error("获取内存信息:" + 0);
        }
        float diskInfos = 0f;
        try {
            diskInfos = NetWorkUtil.getDiskInfo();
            log.info("获取硬盘信息:" + diskInfos);
        } catch (IOException e) {
            log.error("获取硬盘信息:" + e.getMessage());
        } catch (InterruptedException e) {
            log.error("获取硬盘信息:" + e.getMessage());
        }
        DeviceStatusRespVo deviceStatusRespVo = new DeviceStatusRespVo();
        deviceStatusRespVo.setCpuRate(cpuInfo);
        deviceStatusRespVo.setMemoryRate(memInfo);
        deviceStatusRespVo.setDisRate(diskInfos);
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
    public ResultVo<EvictingQueue<NetSpeedRespVo>> getNetSpeed() {
        log.info("获取网络吞吐量");
        Map<String, String> netWorkDownUp = NetWorkUtil.getNetWorkDownUp();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(StaticValue.TIME_FORMAT);
        if (queue.size() == 0) {
            Integer length = 10;
            for (int j = StaticValue.LENGTH; j > 0; j--) {
                NetSpeedRespVo netSpeedRespVo;
                if (j == 1) {
                    netSpeedRespVo = new NetSpeedRespVo();
                    netSpeedRespVo.setDateTime(now.format(dateTimeFormatter));
                    netSpeedRespVo.setDownSpeed(netWorkDownUp.get(StaticValue.RX_PERCENT));
                    netSpeedRespVo.setUpSpeed(netWorkDownUp.get(StaticValue.TX_PERCENT));
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
        netSpeedRespVo.setDownSpeed(netWorkDownUp.get(StaticValue.RX_PERCENT));
        netSpeedRespVo.setUpSpeed(netWorkDownUp.get(StaticValue.TX_PERCENT));
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
    public ResultVo<IndexVo> ldapInfo() {
        IndexVo indexVo = new IndexVo();
        Field[] fields = IndexVo.class.getDeclaredFields();

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //根据字段名称查询对应的总数
        for (Field field : fields) {
            System.out.println(field.getName());
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                return queryFieldNum(field, indexVo);
            });
            futures.add(future);
        }
        // 使用allOf方法来表示所有的并行任务
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()]));

        // 下面的方法可以帮助我们获得所有子任务的处理结果
        CompletableFuture<List<Long>> finalResults = allFutures.thenApply(v ->
                futures.stream().map(CompletableFuture::join).collect(Collectors.toList())
        );
        //等待返回数据
        List<Long> resultList = finalResults.join();
        System.out.println(resultList);
        return ResultUtil.success(indexVo);
    }

    /**
     * 根据字段名称查询对应的总数
     *
     * @param field   字段名称
     * @param indexVo 返回的实体
     * @return
     */
    private long queryFieldNum(Field field, IndexVo indexVo) {
        switch (field.getName()) {
            case StaticValue.TOTAL:
                //查询总数
                long queryTotal = 0;
                queryTotal = queryTotal();
                System.out.println(queryTotal);
                indexVo.setTotal(queryTotal);
                return queryTotal;
            case StaticValue.CERT_TOTAL:
                //查询CERT 总数
                long queryCertTotal = queryCertTotal();
                System.out.println(queryCertTotal);
                indexVo.setCertTotal(queryCertTotal);
                return queryCertTotal;
            case StaticValue.CRL_TOTAL:
                //查询CRL 总数
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
        long crlTotal = LdapUtil.queryTotal(ldapTemplate, ldapSearchFilter, ldapSearchBase, "ou=crl", "ou=cacrl");
        return crlTotal;
    }

    /**
     * 查询CERT接口
     *
     * @return
     */

    private long queryCertTotal() {
        System.out.println("查询CERT接口");
        long certTotal = LdapUtil.queryTotal(ldapTemplate, ldapSearchFilter, ldapSearchBase, "serialNumber=");
        return certTotal;
    }

    /**
     * 查询总数接口
     *
     * @return
     */
    private long queryTotal() {
        System.out.println("查询总数接口");
        long certTotal = LdapUtil.queryTotal(ldapTemplate, ldapSearchFilter, ldapSearchBase, null);
        return certTotal;
    }


}




