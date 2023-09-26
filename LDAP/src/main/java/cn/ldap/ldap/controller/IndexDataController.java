package cn.ldap.ldap.controller;

import cn.ldap.ldap.common.aop.annotations.OperateAnnotation;
import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.dto.RefreshIndexDto;
import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;
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
    @OperateAnnotation(operateModel = OperateMenuEnum.INDEX, operateType = OperateTypeEnum.INDEX_UPDATE_OR_INTER)
    public ResultVo<Boolean> updateIndexData(@RequestBody IndexDataDto indexDataDto) {
       return indexDataService.updateIndexData(indexDataDto);
    }
    @PostMapping("query")
    public ResultVo<List<IndexDataModel>> query() {
        return ResultUtil.success(indexDataService.list());
    }
    @PostMapping("delete")
    @OperateAnnotation(operateModel = OperateMenuEnum.INDEX, operateType = OperateTypeEnum.INDEX_DELETE)
    public ResultVo<Boolean> deleteById(@RequestBody List<Integer> idList){
        return indexDataService.deleteById(idList);
    }

    @PostMapping("queryStatus")
    //@OperateAnnotation(operateModel = OperateMenuEnum.INDEX, operateType = OperateTypeEnum.INDEX_DELETE)
    public ResultVo<Integer> queryStatus(){
        return indexDataService.queryStatus();
    }




    @PostMapping("refreshIndex")
    //@OperateAnnotation(operateModel = OperateMenuEnum.INDEX, operateType = OperateTypeEnum.INDEX_DELETE)
    public ResultVo refreshIndex(@RequestBody RefreshIndexDto refreshIndexDto){
        return indexDataService.refreshIndex(refreshIndexDto);
    }

}
