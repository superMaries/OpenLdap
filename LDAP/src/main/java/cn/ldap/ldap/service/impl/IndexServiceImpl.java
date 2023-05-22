package cn.ldap.ldap.service.impl;

import cn.hutool.log.Log;
import cn.ldap.ldap.common.dto.CertTreeDto;
import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.exception.SysException;
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
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.print.attribute.standard.Finishings;
import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;
import static cn.ldap.ldap.common.enums.ExceptionEnum.NOT_DIRECTORY;

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


    @Resource
    private CertTreeServiceImpl certTreeService;


    @Value("${command.binFile}")
    private String binFile;

    @Value("${ldap.userDn}")
    private String account;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.searchBase}")
    private String searchBase;

    private static final String FRONT_COMMAND = "./ldapsearch -D ";

    private static final String CRL_FILTER = "(&(objectClass=*)(|(cn=crl*)(cn=cacrl*)))";

    private static final String CERT_FILTER = "(&(objectClass=*)(serialNumber=*))";

    private static final String ALL_FILTER = "(objectClass=*)";

    private static final String BEHIND_COMMAND= " |grep \"#\" |wc -l ";

    private String FEED = " ";

    private static final String CD = "cd ";
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
        deviceStatusRespVo.setDisRate(diskInfos * StaticValue.DISK_INFO_NUM);
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
    public ResultVo<IndexVo> ldapInfo(CertTreeDto tree) {
        IndexVo indexVo = new IndexVo();
        Field[] fields = IndexVo.class.getDeclaredFields();

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //根据字段名称查询对应的总数
        for (Field field : fields) {
            System.out.println(field.getName());
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                return queryFieldNum(field, indexVo, tree);
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
     * 查询证书接口
     *
     * @return 查询证书接口
     */
    @Override
    public ResultVo<Long> ldapCrlNum(CertTreeDto tree) {
        Long result = 0L;
        try {
            log.info("切换到可执行命令文件夹:{}",binFile);
            File file = new File(binFile);
            if (!file.exists()){
                throw new SysException(FILE_NOT_EXIST);
            }
            if (!file.isDirectory()){
                throw new SysException(NOT_DIRECTORY);
            }

            //拼接linux命令
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(FEED).append("-w").append(FEED).append("\"").append(password)
                    .append("\"").append(FEED).append("-b").append(FEED).append("\"").append(searchBase)
                    .append("\"").append(FEED).append("\"").append(CRL_FILTER).append("\"").append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}",stringBuilder);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh","-c",stringBuilder.toString());
            Process exec = builder.start();
            InputStream inputStream = exec.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null){
                Long aLong = Long.valueOf(line.trim());
                log.info("Linux查询数量为:{}",aLong);
                if (aLong < 10){
                    result = 0L;
                }else {
                    result = aLong - 10;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success(result);
      //  return ResultUtil.success(queryCrlTotal(tree));
    }

    /**
     * 查询证书接口
     *
     * @return 查询证书接口
     */
    @Override
    public ResultVo<Long> ldapCertNum(CertTreeDto tree) {
        Long result = 0L;
        try {

            File file = new File(binFile);
            if (!file.exists()){
                throw new SysException(FILE_NOT_EXIST);
            }
            if (!file.isDirectory()){
                throw new SysException(NOT_DIRECTORY);
            }

            //拼接linux命令
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(FEED).append("-w").append(FEED).append("\"").append(password)
                    .append("\"").append(FEED).append("-b").append(FEED).append("\"").append(searchBase)
                    .append("\"").append(FEED).append("\"").append(CERT_FILTER).append("\"").append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}",stringBuilder);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh","-c",stringBuilder.toString());
            Process exec = builder.start();
            InputStream inputStream = exec.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null){
                Long aLong = Long.valueOf(line);
                log.info("Linux查询数量为:{}",aLong);
                if (aLong < 10){
                    result = 0L;
                }else {
                    result = aLong - 10;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success(result);
        //  return ResultUtil.success(queryCertTotal(tree));
    }


    /**
     * 返回ldap 总数接口
     *
     * @return 返回ldap 总数接口
     */
    @Override
    public ResultVo<Long> ldapTotal(CertTreeDto tree) {
        Long result = 0L;
        try {

            File file = new File(binFile);
            if (!file.exists()){
                throw new SysException(FILE_NOT_EXIST);
            }
            if (!file.isDirectory()){
                throw new SysException(NOT_DIRECTORY);
            }

            //拼接linux命令
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(FEED).append("-w").append(FEED).append("\"").append(password)
                    .append("\"").append(FEED).append("-b").append(FEED).append("\"").append(searchBase)
                    .append("\"").append(FEED).append("\"").append(ALL_FILTER).append("\"").append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}",stringBuilder);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh","-c",stringBuilder.toString());
            Process exec = builder.start();
            InputStream inputStream = exec.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null){
                Long aLong = Long.valueOf(line.trim());
                if (aLong < 10){
                    result = 0L;
                }else {
                    result = aLong - 10;
                }
            }

        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success(result);
    }

    /**
     * 根据字段名称查询对应的总数
     *
     * @param field   字段名称
     * @param indexVo 返回的实体
     * @return
     */
    private long queryFieldNum(Field field, IndexVo indexVo, CertTreeDto tree) {
        switch (field.getName()) {
            case StaticValue.TOTAL:
                //查询总数
                long queryTotal = 0;
                queryTotal = queryTotal(tree);
                System.out.println(queryTotal);
                indexVo.setTotal(queryTotal);
                return queryTotal;
            case StaticValue.CERT_TOTAL:
                //查询CERT 总数
                long queryCertTotal = queryCertTotal(tree);
                System.out.println(queryCertTotal);
                indexVo.setCertTotal(queryCertTotal);
                return queryCertTotal;
            case StaticValue.CRL_TOTAL:
                //查询CRL 总数
                long queryCrlTotal = queryCrlTotal(tree);
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
     * @param tree 条件
     * @return
     */
    private long queryCrlTotal(CertTreeDto tree) {
        log.info("查询CRL接口");
        if (ObjectUtils.isEmpty(tree) && ObjectUtils.isEmpty(tree.getBaseDN())) {
            ldapSearchBase = tree.getBaseDN();
        }
        log.info("开始查询CRL，查询条件为:{}",StaticValue.CRL+ StaticValue.CACRL);
     //  log.info("Number of active connections: " + pooledContextSource.getNumActive());
     //   log.info("Number of idle connections: " + pooledContextSource.getNumIdle());
        LdapTemplate newLdapTemplate = certTreeService.fromPool();
        long crlTotal = LdapUtil.queryLdapNum(newLdapTemplate, ldapSearchFilter, ldapSearchBase, StaticValue.CRL, StaticValue.CACRL);

        log.info("查询结束，数量为:{}",crlTotal);

        return crlTotal;
    }

    /**
     * 查询CERT接口
     *
     * @param tree 条件
     * @return
     */

    private long queryCertTotal(CertTreeDto tree) {
        log.info("查询CERT接口");
        if (ObjectUtils.isEmpty(tree) && ObjectUtils.isEmpty(tree.getBaseDN())) {
            ldapSearchBase = tree.getBaseDN();
        }
        log.info("开始查询证书，查询条件为:{}",StaticValue.SERIALNUMBER);
     //   log.info("Number of active connections: " + pooledContextSource.getNumActive());
      //  log.info("Number of idle connections: " + pooledContextSource.getNumIdle());
        LdapTemplate newLdapTemplate = certTreeService.fromPool();

        long certTotal = LdapUtil.queryLdapNum(newLdapTemplate, ldapSearchFilter, ldapSearchBase, StaticValue.SERIALNUMBER);
        log.info("查询结束，数量为:{}",certTotal);
        return certTotal;
    }

    /**
     * 查询总数接口
     *
     * @param tree 条件
     * @return
     */
    private long queryTotal(CertTreeDto tree) {
        System.out.println("查询总数接口");
        if (ObjectUtils.isEmpty(tree) && ObjectUtils.isEmpty(tree.getBaseDN())) {
            ldapSearchBase = tree.getBaseDN();
        }
      //  log.info("Number of active connections: " + pooledContextSource.getNumActive());
       // log.info("Number of idle connections: " + pooledContextSource.getNumIdle());
        LdapTemplate newLdapTemplate = certTreeService.fromPool();

        long certTotal = LdapUtil.queryLdapNum(newLdapTemplate, ldapSearchFilter, ldapSearchBase, null);
        return certTotal;
    }


}




