package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.IndexDataDto;
import cn.ldap.ldap.common.vo.ResultVo;

/**
 * 索引
 *
 * @title: IndexDataController
 * @Author Wy
 * @Date: 2023/4/11 17:26
 * @Version 1.0
 */
public interface IndexDataService {
    /**
     * 更新或者插入
     *
     * @param indexDataDto 参数
     * @return true 成功 false 失败
     */
    ResultVo<Boolean> updateIndexData(IndexDataDto indexDataDto);
}

