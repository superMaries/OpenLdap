package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.IndexDataModel;
import cn.ldap.ldap.common.entity.IndexRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IndexRuleMapper extends BaseMapper<IndexRule> {

    @Select("select * from index_data limit 1")
    IndexDataModel queryStatus();
}
