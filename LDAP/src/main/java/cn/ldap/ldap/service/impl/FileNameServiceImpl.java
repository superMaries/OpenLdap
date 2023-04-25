package cn.ldap.ldap.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.ldap.ldap.common.entity.FileName;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.mapper.FileNameMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.FileNameService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class FileNameServiceImpl extends ServiceImpl<FileNameMapper, FileName> implements FileNameService {
    @Override
    public ResultVo<Object> queryFileName() {
        FileName fileName = getOne(null);
        if (ObjectUtils.isEmpty(fileName.getFileName())){
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }
        log.info("文件路径为:{}",fileName.getFileName());
        Map<String,String> resultMap = new HashMap<>();
        resultMap.put("data",fileName.getFileName());
        return ResultUtil.success(resultMap);
    }
}
