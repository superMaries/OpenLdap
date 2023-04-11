package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.entity.IndexRule;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IndexRuleService extends IService<IndexRule> {
    /**
     * 查询索引规则
     * @return
     */
    ResultVo<List<String>> queryIndexRule();

    /**
     * 开启或者关闭SSL
     * @param serverDto
     * @return
     */
    ResultVo<Object> sslOperation(ServerDto serverDto);
}
