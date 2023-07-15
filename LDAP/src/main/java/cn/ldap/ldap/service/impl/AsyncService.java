package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.service.IndexDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author suntao
 * @create 2023/7/3
 */
@Service
public class AsyncService {

    @Resource
    private IndexDataService indexDataService;

    @Async
    public void asyncMethod(IndexDataModel indexDataModel) {
        indexDataService.updateById(indexDataModel);
    }
}
