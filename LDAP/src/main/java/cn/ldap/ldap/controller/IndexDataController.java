package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexDataService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 索引
 *
 * @title: IndexDataController
 * @Author Wy
 * @Date: 2023/4/11 17:26
 * @Version 1.0
 */
@RestController
@RequestMapping("/indexData/")
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
    @PostMapping("query")
    public ResultVo<List<IndexDataModel>> query() {
        return ResultUtil.success(indexDataService.list());
    }
    @PostMapping("delete/{id}")
    public ResultVo<Boolean> deleteById(@PathVariable Integer id){
        return indexDataService.deleteById(id);
    }

}
