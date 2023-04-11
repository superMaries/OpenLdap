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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.util.List;
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
        List<IndexDataModel> indexDatas = list(new LambdaQueryWrapper<IndexDataModel>()
                .eq(IndexDataModel::getIndexRule, indexDataDto.getIndexRule()));
        String index = indexDatas.stream()
                .map(IndexDataModel::getIndexAttribute)
                .collect(Collectors.toList())
                .stream().map(it -> it + StaticValue.SPLIT).collect(Collectors.joining());
        //修改配置文件
        //判断文件是否存在
        File file = new File("");
        if (!file.exists()) {
            throw new SystemException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(""))) {
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.trim().startsWith("index")
                        || lineStr.trim().endsWith("eq")) {
                    lineStr = "index " + index + " eq";
                }
                String oldData = lineStr;
                stringBuilder.append(oldData);
            }
        } catch (Exception e) {
            return ResultUtil.fail();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("fileName"))) {
            bufferedWriter.write(stringBuilder.toString());
        } catch (Exception e) {
            return ResultUtil.fail();
        }
        return ResultUtil.success();
    }
}
