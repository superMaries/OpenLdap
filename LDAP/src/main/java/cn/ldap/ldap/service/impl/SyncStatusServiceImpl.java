package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.SyncStatusDto;
import cn.ldap.ldap.common.entity.SyncStatus;
import cn.ldap.ldap.common.enums.ConfigEnum;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.SyncStatusMapper;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.hander.InitConfigData;
import cn.ldap.ldap.service.SyncStatusService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.io.File;
import java.io.IOException;
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


    private static final Integer NUM = 0;

    private static final String SYNC = "已同步";

    private static final String NOT_SYNC = "未同步";

    private static final String CONNECTION_FAILD = "连接失败";


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


    @Override
    public ResultVo<Object> mainQuery() {
        //查询数据库中所有 从服务的连接信息
        List<SyncStatus> dataList = list();
        log.info("查询配置数据为:{}", JSON.toJSONString(dataList));
        //判断集合是否为空
        if (CollectionUtils.isEmpty(dataList)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }

        List<SyncStatus> resultList = new ArrayList<>();
        //遍历集合，通过集合获取连接信息，准备连接服务进行查询
        for (SyncStatus syncStatus : dataList) {
            if (ObjectUtils.isEmpty(syncStatus.getFollowServerIp())
                    || ObjectUtils.isEmpty(syncStatus.getSyncPoint())
                    || ObjectUtils.isEmpty(syncStatus.getAccount()) || ObjectUtils.isEmpty(syncStatus.getPassword())) {
                return ResultUtil.fail(ExceptionEnum.LDAP_DATA_ERROR);
            }
            //连接服务
            //查询主服务数据，判断连接状态，并且分别插入到返回值中
            Map<String, Object> mainMap = new HashMap<>();
            mainMap = LdapUtil.queryTreeRdnOrNum(mainMap, ldapTemplate, SCOPE, syncStatus.getSyncPoint(), FILTER);
            Integer mainCount = Integer.valueOf(mainMap.get(RDN_CHILD_NUM).toString());
            syncStatus.setMainServerNumber(mainCount);
            //设置从服务数据初始值
            Integer followCount = 0;
            //查询从服务数据，判断连接状态，并且分别插入到返回值中
           try {
               LdapTemplate connection = connection(syncStatus.getFollowServerIp(), syncStatus.getSyncPoint(), syncStatus.getAccount(), syncStatus.getPassword());
               Map<String, Object> followMap = new HashMap<>();
               followMap = LdapUtil.queryTreeRdnOrNum(followMap, connection, SCOPE, syncStatus.getSyncPoint(), FILTER);
               followCount = Integer.valueOf(followMap.get(RDN_CHILD_NUM).toString());
           }catch (Exception e){
               followCount = NUM;
               syncStatus.setFollowServerNumber(followCount);
               syncStatus.setSyncStatusStr(CONNECTION_FAILD);
               resultList.add(syncStatus);
               continue;
           }
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
        }
        return ResultUtil.success(resultList);
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
            searchbase = section.get("searchbase");
            binddn = section.get("binddn");
            credentials = section.get("credentials");
        } catch (IOException e) {
            log.error("修改文件异常:{}", e.getMessage());
            return ResultUtil.fail(ExceptionEnum.FILE_IO_ERROR);
        }
        SyncStatus syncStatus = new SyncStatus();
        //连接服务
        LdapTemplate connection = connection(provider, searchbase, binddn, credentials);
        //查询主服务数据，判断连接状态，并且分别插入到返回值中
        Map<String, Object> mainMap = new HashMap<>();
        mainMap = LdapUtil.queryTreeRdnOrNum(mainMap, ldapTemplate, SCOPE, syncStatus.getSyncPoint(), FILTER);
        Integer mainCount = Integer.valueOf(mainMap.get(RDN_CHILD_NUM).toString());
        syncStatus.setMainServerNumber(mainCount);
        //查询从服务数据，判断连接状态，并且分别插入到返回值中
        Map<String, Object> followMap = new HashMap<>();
        followMap = LdapUtil.queryTreeRdnOrNum(followMap, connection, SCOPE, syncStatus.getSyncPoint(), FILTER);

        Integer followCount = Integer.valueOf(followMap.get(RDN_CHILD_NUM).toString());
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
        Map<String, String> map = new HashMap<>();
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
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
            }
        } else {
            throw new SysException(ExceptionEnum.SYSTEM_CONFIG_ERRROR);
        }
        return ResultUtil.success(map);

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
