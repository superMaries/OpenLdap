package cn.ldap.ldap.service.impl;

import cn.hutool.json.JSONUtil;
import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.dto.RefreshIndexDto;
import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.common.entity.IndexRule;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.IndexDataMapper;
import cn.ldap.ldap.common.mapper.IndexRuleMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexDataService;
import cn.ldap.ldap.service.IndexRuleService;
import cn.ldap.ldap.service.LdapConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;
import static cn.ldap.ldap.common.enums.ExceptionEnum.valueOf;

/**
 * @title: IndexDataServiceImpl
 * @Author Wy
 * @Date: 2023/4/11 17:34
 * @Version 1.0
 */
@Service
@Slf4j
public class IndexDataServiceImpl extends ServiceImpl<IndexDataMapper, IndexDataModel> implements IndexDataService {

    @Resource
    private AsyncService asyncService;
    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;

    @Value("${filePath.indexPath}")
    private String indexPath;

    private static final String VV = " -o ";

    private static final String START_WITH = "index";

    private static final String OBJECTCLASS = "objectClass";

    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    @Resource
    private LdapConfigServiceImpl ldapConfigServiceImpl;


    /**
     * 换行
     */
    private static final String FEED = "\n";

    @Resource
    private IndexRuleMapper indexRuleMapper;

    private static final Integer NOT_REFRESH = 0;

    private static final Integer REFRESH_ING = 1;

    private static final Integer REFRESH_DONE = 2;

    private static final String REFRESH_COMMAND = "cd ";

    private static final String REFRESH = "; ./slapindex -f ";

    private static final String RESTART_COMMAND = "; ./slapindex -v -f ";

    private static final String SERVICE_NAME = "slapd.service";

    private static final String SLAP_INDEX = "slapindex";

    private static final String DELETE_END = " >>../var/logs/ldap.log";

    /**
     * 更新或者插入
     *
     * @param indexDataDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> updateIndexData(IndexDataDto indexDataDto) {
        if (ObjectUtils.isEmpty(indexDataDto)
                || ObjectUtils.isEmpty(indexDataDto.getIndexRule())
                || ObjectUtils.isEmpty(indexDataDto.getAttributeName())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        //先判断服务是否关闭，如果服务关闭返回信息请关闭服务
        Boolean aBoolean = ldapConfigServiceImpl.linuxCommand(SERVICE_NAME);
        if (aBoolean) {
            return ResultUtil.fail(ExceptionEnum.SERVICE_NEED_CLOSE);
        }
        //判断当前进程中是否有slapindex进程在运行
        Boolean isRunning = false;
        try {
            Process exec = Runtime.getRuntime().exec("ps -ef | grep " + SLAP_INDEX);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(SLAP_INDEX)) {
                    isRunning = true;
                    break;
                }
            }

        } catch (IOException e) {
            throw new SysException(e.getMessage());
        }
        //如果有正在运行的程序会报错
        if (isRunning) {
            return ResultUtil.fail(ExceptionEnum.INDEX_IS_RUNNING);
        }


        List<IndexDataModel> indexDataModels = null;
        indexDataModels = list(new LambdaQueryWrapper<IndexDataModel>()
                .eq(!ObjectUtils.isEmpty(indexDataDto.getAttributeName()), IndexDataModel::getIndexAttribute, indexDataDto.getAttributeName())
                .eq(!ObjectUtils.isEmpty(indexDataDto.getIndexRule()), IndexDataModel::getIndexRule, indexDataDto.getIndexRule())
                .ne(!ObjectUtils.isEmpty(indexDataDto.getId()), IndexDataModel::getId, indexDataDto.getId()));
        if (indexDataModels.size() >= StaticValue.COUNT) {
            log.info(ExceptionEnum.DATA_EXIT.getMessage());
            return ResultUtil.fail(ExceptionEnum.DATA_EXIT);
        }
        QueryWrapper<IndexRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IndexRule::getType, indexDataDto.getIndexRule());
        IndexRule indexRule = indexRuleMapper.selectOne(queryWrapper);
        //新增
        if (ObjectUtils.isEmpty(indexDataDto.getId())) {
            IndexDataModel indexDataModel = new IndexDataModel();
            indexDataModel.setIndexAttribute(indexDataDto.getAttributeName());
            indexDataModel.setIndexRule(indexDataDto.getIndexRule());
            indexDataModel.setDescription(indexRule.getDescription());
            indexDataModel.setStatus(NOT_REFRESH);
            saveOrUpdate(indexDataModel);
        }
        //根据规则拿到对应数据
        //更改全部数据

        return getData();
    }

    @Override
    public ResultVo<Boolean> deleteById(List<Integer> idList) {

        log.info("删除索引接口参数为:{}", idList);

        //查询数据库中是否有更新中的索引，如果有的话不让删除
        List<IndexDataModel> list = list();
        if (!CollectionUtils.isEmpty(list)) {
            for (IndexDataModel indexDataModel : list) {
                if (REFRESH_ING.equals(indexDataModel.getStatus())) {
                    return ResultUtil.fail(ExceptionEnum.INDEX_IS_RUNNING);
                }
            }
        }
        //先判断服务是否关闭，如果服务关闭返回信息请关闭服务
        Boolean aBoolean = ldapConfigServiceImpl.linuxCommand(SERVICE_NAME);
        if (aBoolean) {
            return ResultUtil.fail(ExceptionEnum.SERVICE_NEED_CLOSE);
        }
        //判断当前进程中是否有slapindex进程在运行
        Boolean isRunning = false;
        try {
            Process exec = Runtime.getRuntime().exec("ps -ef | grep " + SLAP_INDEX);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(SLAP_INDEX)) {
                    isRunning = true;
                    break;
                }
            }

        } catch (IOException e) {
            throw new SysException(e.getMessage());
        }
        //如果有正在运行的程序会报错
        if (isRunning) {
            return ResultUtil.fail(ExceptionEnum.INDEX_IS_RUNNING);
        }

        //根据列表数据更改状态为删除中
        List<IndexDataModel> list1 = list(new QueryWrapper<IndexDataModel>().lambda().in(IndexDataModel::getId, idList));
        for (IndexDataModel indexDataModel : list1) {
            indexDataModel.setStatus(4);
        }
        updateBatchById(list1);
        return getDataA(idList);
    }

    @Override
    public ResultVo<Integer> queryStatus() {
        IndexDataModel indexDataModel = indexRuleMapper.queryStatus();
        if (ObjectUtils.isEmpty(indexDataModel)) {
            return ResultUtil.success(NOT_REFRESH);
        }
        return ResultUtil.success(indexDataModel.getStatus());
    }

    @Override
    public ResultVo refreshIndex(RefreshIndexDto refreshIndexDto) {

        QueryWrapper<IndexDataModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IndexDataModel::getId, refreshIndexDto.getId());
        queryWrapper.lambda().eq(IndexDataModel::getIndexAttribute, refreshIndexDto.getIndex());
        IndexDataModel one = getOne(queryWrapper);
        if (ObjectUtils.isEmpty(one)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }
        log.info("查询到到数据为:{}", one);
        one.setStatus(REFRESH_ING);
        log.info("修改后的信息:{}", JSONUtil.toJsonStr(one));
        boolean b = updateById(one);
        log.info("修改返回值:{}", b);

        log.info("即将执行Linux命令---------------------------------！！！");

        String command = REFRESH_COMMAND + indexPath + REFRESH + configPath + VV + refreshIndexDto.getIndex();
        ProcessBuilder builder = new ProcessBuilder();
        log.info("刷新命令为:{}", command);
        builder.command("sh", "-c", command);
        try {
            log.info("刷新命令准备开始执行---------------------------" + new Date());
            Process start = builder.start();
            int i = start.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(start.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("刷新索引输出:{}", line);
            }
            log.info("刷新命令执行结束------------------------------" + new Date());
            log.info("状态码--------:{}", i);


            log.info("刷新索引命令结束:{}", command);
            //修改数据库数据
            log.info("准备修改数据库数据");
            if (i == 0) {
                QueryWrapper<IndexDataModel> twiceQueryWrapper = new QueryWrapper<>();
                twiceQueryWrapper.lambda().eq(IndexDataModel::getId, refreshIndexDto.getId());
                twiceQueryWrapper.lambda().eq(IndexDataModel::getIndexAttribute, refreshIndexDto.getIndex());
                IndexDataModel two = getOne(twiceQueryWrapper);
                two.setStatus(REFRESH_DONE);
                saveOrUpdate(two);
                log.info("修改数据库结束:{}", two);
            }else{
                throw new SysException(ExceptionEnum.REFRESH_ERROR);
            }

        } catch (IOException e) {
            log.error("索引刷新失败");
            throw new SysException(ExceptionEnum.REFRESH_ERROR);
        } catch (InterruptedException e) {
            throw new SysException(e.getMessage());
        }
        return ResultUtil.success();
    }


    public ResultVo<Boolean> getData() {
        List<String> stringList = new ArrayList<>();
        List<IndexDataModel> indexDatas = this.list();

        String str = "";
        if (!CollectionUtils.isEmpty(indexDatas)) {

            Map<String, List<IndexDataModel>> listMap = indexDatas.stream().collect(Collectors.groupingBy(IndexDataModel::getIndexRule));
            Set<String> strings = listMap.keySet();
            for (String string : strings) {
                String collect = listMap.get(string).stream()
                        .map(IndexDataModel::getIndexAttribute)
                        .collect(Collectors.toList())
                        .stream().map(it -> it + StaticValue.SPLIT).collect(Collectors.joining());
                stringList.add(START_WITH + SPACE_DATA + collect.substring(StaticValue.SPLIT_COUNT, collect.length() - 1) + SPACE_DATA + string + FEED);
            }
            str = stringList.stream().collect(Collectors.joining());
        } else {
            str = "";
        }


        //  String str =
        //修改配置文件
        String fileName = configPath;
        //判断文件是否存在
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String lineStr = null;
            boolean is = false;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.startsWith(START_WITH)) {
                    if (lineStr.trim().contains(OBJECTCLASS)) {
                        String oldData = lineStr;
                        stringBuilder.append(oldData).append(FEED);
                        is = true;
                    } else {
                        continue;
                    }
                }
                if (is) {
                    String oldData = str;
                    stringBuilder.append(oldData).append(FEED);
                    is = false;
                    continue;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }
        } catch (Exception e) {
            return ResultUtil.fail();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            bufferedWriter.write(stringBuilder.toString());

        } catch (Exception e) {
            return ResultUtil.fail();
        }
        return ResultUtil.success();
    }

    public ResultVo<Boolean> getDataA(List<Integer> idList) {

        List<String> stringList = new ArrayList<>();
        List<IndexDataModel> indexDatas = this.list(new QueryWrapper<IndexDataModel>().lambda().ne(IndexDataModel::getStatus,4));

        String str = "";
        if (!CollectionUtils.isEmpty(indexDatas)) {

            Map<String, List<IndexDataModel>> listMap = indexDatas.stream().collect(Collectors.groupingBy(IndexDataModel::getIndexRule));
            Set<String> strings = listMap.keySet();
            for (String string : strings) {
                String collect = listMap.get(string).stream()
                        .map(IndexDataModel::getIndexAttribute)
                        .collect(Collectors.toList())
                        .stream().map(it -> it + StaticValue.SPLIT).collect(Collectors.joining());
                stringList.add(START_WITH + SPACE_DATA + collect.substring(StaticValue.SPLIT_COUNT, collect.length() - 1) + SPACE_DATA + string + FEED);
            }
            str = stringList.stream().collect(Collectors.joining());
        } else {
            str = "";
        }

        //  String str =
        //修改配置文件
        String fileName = configPath;
        //判断文件是否存在
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String lineStr = null;
            boolean is = false;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.startsWith(START_WITH)) {
                    if (lineStr.trim().contains(OBJECTCLASS)) {
                        String oldData = lineStr;
                        stringBuilder.append(oldData).append(FEED);
                        is = true;
                    } else {
                        continue;
                    }
                }
                if (is) {
                    String oldData = str;
                    stringBuilder.append(oldData).append(FEED);
                    is = false;
                    continue;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }
        } catch (Exception e) {
            return ResultUtil.fail();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            log.info("删除索引接口写入配置文件");
            bufferedWriter.write(stringBuilder.toString());
            log.info("删除索引接口写入conf配置文件流关闭");
            bufferedWriter.close();


            log.info("删除索引接口，即将执行Linux命令---------------------------------！！！");

            String command = REFRESH_COMMAND + indexPath + RESTART_COMMAND + configPath+ DELETE_END;
            ProcessBuilder builder = new ProcessBuilder();
            log.info("刷新命令为:{}", command);
            builder.command("sh", "-c", command);
            try {
                log.info("刷新命令准备开始执行---------------------------" + System.currentTimeMillis());
                Process start = builder.start();
                int i = start.waitFor();
                //BufferedReader reader = new BufferedReader(new InputStreamReader(start.getInputStream()));
              //  String line;
//                while ((line = reader.readLine()) != null) {
//                    log.info("刷新索引输出:{}", line);
//                }
                log.info("刷新命令执行结束------------------------------" + System.currentTimeMillis());
                log.info("状态码--------:{}", i);
                log.info("刷新索引命令结束:{}", command);

             //   reader.close();
                //根据列表先删除数据库中索引的数据
                log.info("删除索引接口开始删除数据表数据");
               if (0 == i ){
                   boolean removeResult = removeByIds(idList);
                   if (!removeResult) {
                       return ResultUtil.fail("删除索引失败");
                   }
               }else {
                   return ResultUtil.fail("删除索引失败");
               }

            } catch (Exception e) {
                return ResultUtil.fail();
            }
            return ResultUtil.success();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}