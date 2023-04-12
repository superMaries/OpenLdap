package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.IndexDataMapper;
import cn.ldap.ldap.common.mapper.PermissionMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

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
        //新增
        if (ObjectUtils.isEmpty(indexDataDto.getId())) {
            IndexDataModel indexDataModel = new IndexDataModel();
            indexDataModel.setIndexAttribute(indexDataDto.getAttributeName());
            indexDataModel.setIndexRule(indexDataDto.getIndexRule());
            saveOrUpdate(indexDataModel);
        }
        //根据规则拿到对应数据

        return getData();
    }

    @Override
    public ResultVo<Boolean> deleteById(Integer id) {
        removeById(id);
        return getData();
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
            throw new SystemException(FILE_NOT_EXIST);
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
        return ResultUtil.success();
    }
}
