package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.ldap.ldap.common.dto.QueryFollowNumDto;
import cn.ldap.ldap.common.dto.SyncStatusDto;
import cn.ldap.ldap.common.entity.SyncStatus;
import cn.ldap.ldap.common.enums.ConfigEnum;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.SyncStatusMapper;
import cn.ldap.ldap.common.util.IscSignUtil;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.hander.InitConfigData;
import cn.ldap.ldap.service.SyncStatusService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class SyncStatusServiceImpl extends ServiceImpl<SyncStatusMapper, SyncStatus> implements SyncStatusService {

    @Resource
    private LdapTemplate ldapTemplate;

    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;

    private static final Integer SCOPE = 2;

    private static final String FILTER = "(objectClass=*)";

    private static final String RDN_CHILD_NUM = "rdnChildNum";


    private static final Long NUM = 0L;

    private static final String SYNC = "已同步";

    private static final String NOT_SYNC = "未同步";

    private static final String CONNECTION_FAILD = "连接失败";

    @Resource
    private CertTreeServiceImpl certTreeService;

    @Resource
    private IndexServiceImpl indexService;

    @Value("${command.binFile}")
    private String binFile;

    private static final String FRONT_COMMAND = "./ldapsearch -H ";


    private static final String AND_D = "-D";

    private static final String ALL_FILTER = "(objectClass=*)";

    private static final String BEHIND_COMMAND= " |grep \"#\" |wc -l ";

    private String FEED = " ";

    private static final String CD = "cd ";

    private static final String CA_CER = "ca.cer";

    private static final String SERVER_CER = "server.cer";

    private static final String SERVER_KEY = "server.key";

    @Value("${filePath.followCertPath}")
    private String followPath;


    @Value("${ldap.userDn}")
    private String rootAccount;

    @Value("${ldap.password}")
    private String rootPassword;




    /**
     * 添加从服务配置信息
     *
     * @param syncStatusDto
     */
    @Override
    public ResultVo<Object> add(SyncStatusDto syncStatusDto) {
        if (ObjectUtils.isEmpty(syncStatusDto)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_EMPTY);
        }
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setFollowServerIp(syncStatusDto.getUrl());
        syncStatus.setAccount(syncStatusDto.getAccount());
        syncStatus.setPassword(syncStatusDto.getPassword());
        syncStatus.setSyncPoint(syncStatusDto.getSyncPoint());
        syncStatus.setCreateTime(new Date().toString());
        save(syncStatus);
        return ResultUtil.success();
    }

    /**
     * 修改连接信息
     *
     * @param syncStatusDto
     * @return
     */
    @Override
    public ResultVo<Object> update(SyncStatusDto syncStatusDto) {
        if (ObjectUtils.isEmpty(syncStatusDto.getUrl()) || ObjectUtils.isEmpty(syncStatusDto.getAccount())
                || ObjectUtils.isEmpty(syncStatusDto.getPassword()) || ObjectUtils.isEmpty(syncStatusDto.getSyncPoint())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        QueryWrapper<SyncStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SyncStatus::getId, syncStatusDto.getId());
        SyncStatus one = getOne(queryWrapper);
        if (ObjectUtils.isEmpty(one)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }
        one.setSyncPoint(syncStatusDto.getSyncPoint());
        one.setAccount(syncStatusDto.getAccount());
        one.setPassword(syncStatusDto.getPassword());
        one.setFollowServerIp(syncStatusDto.getUrl());
        updateById(one);
        return ResultUtil.success();
    }

//    @Override
//    public ResultVo<Object> mainQuery() {
//        //查询数据库中所有 从服务的连接信息
//        List<SyncStatus> dataList = list();
//        log.info("查询配置数据为:{}", JSON.toJSONString(dataList));
//        //判断集合是否为空
//        if (CollectionUtils.isEmpty(dataList)) {
//            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
//        }
//
//        List<SyncStatus> resultList = new ArrayList<>();
//        //遍历集合，通过集合获取连接信息，准备连接服务进行查询
//        for (SyncStatus syncStatus : dataList) {
//            if (ObjectUtils.isEmpty(syncStatus.getFollowServerIp())
//                    || ObjectUtils.isEmpty(syncStatus.getSyncPoint())
//                    || ObjectUtils.isEmpty(syncStatus.getAccount()) || ObjectUtils.isEmpty(syncStatus.getPassword())) {
//                return ResultUtil.fail(ExceptionEnum.LDAP_DATA_ERROR);
//            }
//            //连接服务
//            //查询主服务数据，判断连接状态，并且分别插入到返回值中
//            Map<String, Object> mainMap = new HashMap<>();
//            LdapTemplate newLdapTemplate = certTreeService.fromPool();
//            mainMap = LdapUtil.queryTreeRdnOrNumEx(mainMap, newLdapTemplate, SCOPE, syncStatus.getSyncPoint(), FILTER);
//            Integer mainCount = Integer.valueOf(mainMap.get(RDN_CHILD_NUM).toString());
//            syncStatus.setMainServerNumber(mainCount);
//            //设置从服务数据初始值
//            Integer followCount = 0;
//            //查询从服务数据，判断连接状态，并且分别插入到返回值中
//            try {
//                LdapTemplate connection = connection(syncStatus.getFollowServerIp(), syncStatus.getSyncPoint(), syncStatus.getAccount(), syncStatus.getPassword());
//                Map<String, Object> followMap = new HashMap<>();
//                followMap = LdapUtil.queryTreeRdnOrNumEx(followMap, connection, SCOPE, syncStatus.getSyncPoint(), FILTER);
//                followCount = Integer.valueOf(followMap.get(RDN_CHILD_NUM).toString());
//            }catch (Exception e){
//                followCount = NUM;
//                syncStatus.setFollowServerNumber(followCount);
//                syncStatus.setSyncStatusStr(CONNECTION_FAILD);
//                resultList.add(syncStatus);
//                continue;
//            }
//            syncStatus.setFollowServerNumber(followCount);
//            if (followCount.equals(NUM)) {
//                syncStatus.setSyncStatusStr(CONNECTION_FAILD);
//            }
//            if (mainCount.equals(followCount)) {
//                syncStatus.setSyncStatusStr(SYNC);
//            } else {
//                syncStatus.setSyncStatusStr(NOT_SYNC);
//            }
//            resultList.add(syncStatus);
//        }
//        return ResultUtil.success(resultList);
//    }


    @Override
    public ResultVo<Object> mainQuery() {
        //查询数据库中所有 从服务的连接信息


        List<SyncStatus> dataList = list();
        for (SyncStatus syncStatus : dataList) {
            Long aLong = indexService.mainQueryLinux(syncStatus.getSyncPoint());
            syncStatus.setMainServerNumber(aLong);
        }
        return ResultUtil.success(dataList);
    }

    @Override
    public ResultVo<Object> followQuery() {
        List<SyncStatus> resultList = new ArrayList<>();
        Wini wini = null;
        String provider = "";
        String searchbase = "";
        String binddn = "";
        String credentials = "";
        try {
            wini = new Wini(new File(configPath));
            Profile.Section section = wini.get("?");
            provider = section.get("provider");
            searchbase = section.get("searchbase").replace("\"", "");
            binddn = section.get("binddn").replace("\"", "");
            credentials = section.get("credentials").replace("\"", "");
        } catch (IOException e) {
            log.error("修改文件异常:{}", e.getMessage());
            return ResultUtil.fail(ExceptionEnum.FILE_IO_ERROR);
        }
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setMainServerIp(provider);
        syncStatus.setSyncPoint(searchbase);
        syncStatus.setAccount(binddn);
        syncStatus.setPassword(credentials);
        //连接服务

        Long mainCount = followQueryLinux(searchbase, binddn, credentials, provider);


        //查询主服务数据，判断连接状态，并且分别插入到返回值中
//        Map<String, Object> mainMap = new HashMap<>();
//        LdapTemplate connection = connection(provider,searchbase,binddn,credentials);
//        mainMap = LdapUtil.queryTreeRdnOrNumEx(mainMap, connection, SCOPE,searchbase , FILTER);
//        Long mainCount = Long.valueOf(mainMap.get(RDN_CHILD_NUM).toString());
        syncStatus.setMainServerNumber(mainCount);
        //设置从服务数据初始值
        Long followCount = 0L;
        //查询从服务数据，判断连接状态，并且分别插入到返回值中
//        try {
//
//            Map<String, Object> followMap = new HashMap<>();
//            LdapTemplate newLdapTemplate = certTreeService.fromPool();
//            followMap = LdapUtil.queryTreeRdnOrNumEx(followMap, newLdapTemplate, SCOPE, syncStatus.getSyncPoint(), FILTER);
//            followCount = Long.valueOf(followMap.get(RDN_CHILD_NUM).toString());
//        }catch (Exception e){
//            followCount = NUM;
//            syncStatus.setFollowServerNumber(followCount);
//            syncStatus.setSyncStatusStr(CONNECTION_FAILD);
//        }
        followCount = followQueryLinuxSelf(searchbase, rootAccount, rootPassword);
        syncStatus.setFollowServerNumber(followCount);
        if (followCount.equals(NUM)) {
            syncStatus.setSyncStatusStr(CONNECTION_FAILD);
        }
        if (mainCount.equals(followCount)) {
            syncStatus.setSyncStatusStr(SYNC);
        } else {
            syncStatus.setSyncStatusStr(NOT_SYNC);
        }
        resultList.add(syncStatus);

        return ResultUtil.success(resultList);
}

    /**
     * 0, "主服务器"
     * 1, "从服务器"
     *
     * @return
     */
    @Override
    public ResultVo<Map<String, String>> queryServiceConfig() {
        Map<String, Object> map = new HashMap<>();
        if (ConfigEnum.MAIN_SERVICE.getCode().equals(InitConfigData.getServiceType())) {
            //主服务器
            Wini wini = null;
            try {
                wini = new Wini(new File(configPath));
                Profile.Section section = wini.get("?");
                List<String> collect = section.keySet().stream().filter(it -> it.contains("syncprov-checkpoint")).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(collect)) {
                    log.error("系统配置错误,主服务的配置文件有问题");
                    throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
                }

                String msg = collect.get(StaticValue.SPLIT_COUNT);
                String[] split = msg.split(StaticValue.KG);
                String max = split[StaticValue.ONE];
                String credentials = split[StaticValue.TWO];
                map.put("max", max);
                map.put("interval", credentials);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
            }

        } else if (ConfigEnum.FORM_SERVICE.getCode().equals(InitConfigData.getServiceType())) {
            //从服务器
            Wini wini = null;
            try {
                wini = new Wini(new File(configPath));
                Profile.Section section = wini.get("?");
                map.put("interval", section.get("interval"));
                map.put("provider", section.get("provider"));
                map.put("searchbase", section.get("searchbase"));
                map.put("userName", section.get("binddn"));
                map.put("passWord", section.get("credentials"));
                String provider = section.get("provider");
                if (provider.contains("ldaps")){
                    map.put("ifSafe",true);
                    String tlsReqcert = section.get("tls_reqcert");
                    if (tlsReqcert.equals("demand")){
                        map.put("ifBothWay",false);
                        map.put("caCer",followPath+CA_CER);
                        map.put("serverCer",followPath+SERVER_CER);
                        map.put("serverKey",followPath+SERVER_KEY);
                        String caStr = getCerOrKey(followPath+CA_CER);
                        if (!BeanUtil.isEmpty(caStr)){
                            map.put("caCerStr",replaceCer(caStr));
                        }else {
                            map.put("caCerStr","");
                        }


                        String serverStr = getCerOrKey(followPath+SERVER_CER);
                        if (!BeanUtil.isEmpty(serverStr)){
                            map.put("serverCerStr",replaceCer(serverStr));
                        }else {
                            map.put("serverCerStr","");
                        }

                        String keyStr = getCerOrKey(followPath+SERVER_KEY);
                        if (!BeanUtil.isEmpty(keyStr)){
                            map.put("serverKeyStr",keyStr);
                        }else {
                            map.put("serverKeyStr","");
                        }
                    }else {
                        map.put("ifBothWay",true);
                        map.put("caCer",followPath+CA_CER);
                        String cerOrKey = getCerOrKey(followPath+CA_CER);
                        if (!BeanUtil.isEmpty(cerOrKey)){
                            map.put("caCerStr",replaceCer(cerOrKey));
                        }else {
                            map.put("caCerStr","");
                        }
                    }
                }else {
                    map.put("ifSafe",false);
                    map.put("ifBothWay",null);

                }



            } catch (IOException e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
            }
        } else {
            throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
        }
        return ResultUtil.success(map);

    }

    public String getCerOrKey(String name){
        String fileName = name;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileContent = sb.toString();
        return fileContent;

    }

    @Override
    public ResultVo<SyncStatus> queryFollowNum(QueryFollowNumDto queryFollowNumDto) {
        Long result = 0L;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append(queryFollowNumDto.getUrl())
                    .append(FEED).append(AND_D).append(FEED).append("\"").append(queryFollowNumDto.getUserName())
                    .append("\"").append(FEED).append("-w").append(FEED).append("\"").append(queryFollowNumDto.getPassword())
                    .append("\"").append(FEED).append("-b").append(FEED).append("\"").append(queryFollowNumDto.getSyncPoint())
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
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setSyncPoint(queryFollowNumDto.getSyncPoint());
        syncStatus.setAccount(queryFollowNumDto.getUserName());
        syncStatus.setPassword(queryFollowNumDto.getPassword());
        syncStatus.setMainServerNumber(queryFollowNumDto.getMainCount());
        if (result.equals(0L) && queryFollowNumDto.getMainCount().equals(0L)){
            syncStatus.setSyncStatusStr(SYNC);
        }else if ((!result.equals(0L) && queryFollowNumDto.getMainCount().equals(0L)) || ( result.equals(0L) && !queryFollowNumDto.getMainCount().equals(0L))){
            syncStatus.setSyncStatusStr(CONNECTION_FAILD);
        }else if (result.equals(queryFollowNumDto.getMainCount())){
            syncStatus.setSyncStatusStr(SYNC);
        }else {
            syncStatus.setSyncStatusStr(NOT_SYNC);
        }
        syncStatus.setFollowServerNumber(result);
        syncStatus.setFollowServerIp(queryFollowNumDto.getUrl());
        return ResultUtil.success(syncStatus);
    }


    public Long followQueryLinux(String searchBase,String account,String password,String url){
        Long result = 0L;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append(url).append(FEED).append("-D ").append("\"").append(account)
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
        return result;
    }

    public Long followQueryLinuxSelf(String searchBase,String account,String password){
        Long result = 0L;
        try {
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
        return result;
    }

    public static String replaceCer(String cer){
        String s = cer.replaceAll("\n", "").replaceAll(" ", "")
                .replaceAll("BEGIN", "").replaceAll("END", "")
                .replaceAll("-", "").replaceAll("CERTIFICATE", "");
        return s;
    }

    /**
     * 连接
     *
     * @param url
     * @param baseDN
     * @param account
     * @param password
     * @return
     */
    public LdapTemplate connection(String url, String baseDN, String account, String password) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase("");
        contextSource.setUserDn(account);
        contextSource.setPassword(password);
        contextSource.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }
}
