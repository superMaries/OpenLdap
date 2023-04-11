package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexDataService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 索引
 *
 * @title: IndexDataController
 * @Author Wy
 * @Date: 2023/4/11 17:26
 * @Version 1.0
 */
@RestController
@RequestMapping("indexData")
public class IndexDataController {

    @Resource
    private IndexDataService indexDataService;

    /**
     * 更新或者插入
     *
     * @param indexDataDto 参数
     * @return true 成功 false 失败
     */
    @PostMapping("update")
    public ResultVo<Boolean> updateIndexData(@RequestBody IndexDataDto indexDataDto) {
       return indexDataService.updateIndexData(indexDataDto);
    }
}
