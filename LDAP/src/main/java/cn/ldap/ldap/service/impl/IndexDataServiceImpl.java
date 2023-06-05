package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.IndexDataDto;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;

    private static final String START_WITH = "index";

    private static final String OBJECTCLASS = "objectClass";

    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";


    /**
     * 换行
     */
    private static final String FEED = "\n";

    @Resource
    private IndexRuleMapper indexRuleMapper;

    private static final Integer NOT_REFRESH = 0;

    private static final Integer REFRESH_ING = 1;

    private static final Integer REFRESH_DONE = 2;

    private static final String REFRESH_COMMAND = "cd /usr/local/openldap/sbin; ./slapindex -v";

    private static final String RESTART_COMMAND = "systemctl restart slapd.service";

    @Resource
    private IndexDataMapper indexDataMapper;

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
        queryWrapper.lambda().eq(IndexRule::getType,indexDataDto.getIndexRule());
        IndexRule indexRule = indexRuleMapper.selectOne(queryWrapper);
        //新增
        if (ObjectUtils.isEmpty(indexDataDto.getId())) {
            IndexDataModel indexDataModel = new IndexDataModel();
            indexDataModel.setIndexAttribute(indexDataDto.getAttributeName());
            indexDataModel.setIndexRule(indexDataDto.getIndexRule());
            indexDataModel.setDescription(indexRule.getDescription());
            saveOrUpdate(indexDataModel);
        }
        //根据规则拿到对应数据
        //更改全部数据
        List<IndexDataModel> list = this.list();
        if (!CollectionUtils.isEmpty(list)){
            for (IndexDataModel indexDataModel : list) {
                indexDataModel.setStatus(NOT_REFRESH);
            }
        }
        saveOrUpdateBatch(list);

        return getData();
    }

    @Override
    public ResultVo<Boolean> deleteById(Integer id) {
        removeById(id);
        return getData();
    }

    @Override
    public ResultVo<Integer> queryStatus() {
        IndexDataModel indexDataModel = indexRuleMapper.queryStatus();
        if (ObjectUtils.isEmpty(indexDataModel)){
            return ResultUtil.success(NOT_REFRESH);
        }
        return ResultUtil.success(indexDataModel.getStatus());
    }

    @Transactional
    @Override
    public ResultVo refreshIndex() {

        List<IndexDataModel> list = list();
        log.info("查询到的刷新数据的集合:{}",list);

        if (!CollectionUtils.isEmpty(list)){
            for (IndexDataModel indexDataModel : list) {
                indexDataModel.setStatus(REFRESH_ING);
                log.info("修改的状态:{}",REFRESH_ING);
            }
        }
        saveOrUpdateBatch(list);
        log.info("刷新索引第一次保存的集合:{}",list);

        ProcessBuilder builder = new ProcessBuilder();
        log.info("刷新命令为:{}",REFRESH_COMMAND);
        builder.command("sh", "-c", REFRESH_COMMAND);
        try {
            log.info("刷新命令准备开始执行---------------------------");
            Process start = builder.start();
           // int i = start.waitFor();
            log.info("刷新命令执行结束------------------------------");
           // log.info("状态码--------:{}",i);
            // 输出命令执行结果
            //BufferedReader reader = new BufferedReader(new InputStreamReader(start.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                log.info("刷新索引输出:{}",line);
//            }
            List<IndexDataModel> yesList = list;
            log.info("修改为2的集合:{}",yesList);
                for (IndexDataModel indexDataModel : yesList) {
                    indexDataModel.setStatus(REFRESH_DONE);
                    log.info("修改集合2的状态:{}",REFRESH_DONE);
                }
                saveOrUpdateBatch(yesList);
                log.info("修改为2的集合的保存的大小:{}",yesList.size());
                // fooAsync();

//            if (i ==0){
//
//                List<IndexDataModel> yesList = list;
//                for (IndexDataModel indexDataModel : yesList) {
//                    indexDataModel.setStatus(REFRESH_DONE);
//                }
//                saveOrUpdateBatch(yesList);
//               // fooAsync();
//            }
            log.info("刷新索引命令:{}",REFRESH_COMMAND);
        } catch (IOException e) {
            log.error("索引刷新失败");
            throw new SysException(ExceptionEnum.REFRESH_ERROR);
        }
        return ResultUtil.success();
    }

    public CompletableFuture<Object> fooAsync(){
        return CompletableFuture.supplyAsync(()->{
            List<IndexDataModel> list = list();
            for (IndexDataModel indexDataModel : list) {
                indexDataModel.setStatus(REFRESH_DONE);
            }
            saveOrUpdateBatch(list);
            return ResultUtil.success();
        });
    }

    public ResultVo<Boolean> getData(){
        List<String> stringList = new ArrayList<>();
        List<IndexDataModel> indexDatas = this.list();
        Map<String, List<IndexDataModel>> listMap = indexDatas.stream().collect(Collectors.groupingBy(IndexDataModel::getIndexRule));
        Set<String> strings = listMap.keySet();
        for (String string : strings) {
            String collect = listMap.get(string).stream()
                    .map(IndexDataModel::getIndexAttribute)
                    .collect(Collectors.toList())
                    .stream().map(it -> it + StaticValue.SPLIT).collect(Collectors.joining());
            stringList.add( START_WITH + SPACE_DATA + collect.substring(StaticValue.SPLIT_COUNT,collect.length()-1) + SPACE_DATA +string + FEED );
        }

        String str = stringList.stream().collect(Collectors.joining());
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
            boolean is=false;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.startsWith(START_WITH)) {
                    if (lineStr.trim().contains(OBJECTCLASS)) {
                        String oldData = lineStr;
                        stringBuilder.append(oldData).append(FEED);
                        is=true;
                    }else {
                        continue;
                    }
                }
                if (is){
                    String oldData = str;
                    stringBuilder.append(oldData).append(FEED);
                    is=false;
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
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", RESTART_COMMAND);
        log.info("启动命令:{}",RESTART_COMMAND);
        try {
            builder.start();
        } catch (IOException e) {
            log.error("启动命令:{}",e);
            throw new SysException(ExceptionEnum.START_ERROR);
        }
        return ResultUtil.success();
    }
}
