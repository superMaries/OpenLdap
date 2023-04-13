package cn.ldap.ldap.common.mapper;

import cn.ldap.ldap.common.entity.OperationLogModel;
import cn.ldap.ldap.common.vo.LogVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @title: OperationMapper
 * @Author Wy
 * @Date: 2023/3/31 16:04
 * @Version 1.0
 */
@Mapper
public interface OperationMapper extends BaseMapper<OperationLogModel> {
    @Select("<script>" +
            "SELECT " +
            " log.id id ,log.client_ip clientIp" +
            " ,log.user_id userId " +
            " ,CASE WHEN log.user_id=0 THEN'Admin' ELSE u.cert_name END  AS userName" +
            " ,log.operate_menu operateMenu,log.operate_type operateType" +
            " ,log.operate_object operateObject,log.operate_state operateState" +
            " ,log.create_time createTime,log.sign_src signSrc,u.sign_cert signCert" +
            " ,log.sign_value signVlue,log.remark remark" +
            " ,log.audit_id auditId,log.fail_code failCode " +
            " ,CASE WHEN log.audit_id=0 THEN'Admin' ELSE u.cert_name END  AS aduitName" +
            " ,log.audit_status auditStatus,log.audit_src auditSrc" +
            " ,log.audit_sign_value auditSignValue,log.audit_time auditTime " +
            " FROM operation_log log" +
            " LEFT JOIN user u on log.user_id=u.id" +
            " WHERE log.create_time BETWEEN #{beginTime} and #{endTime} " +
            " ORDER BY log.create_time DESC" +
            " LIMIT #{pagePage},#{pageSize}" +
            "</script>")
    List<LogVo> queryLog(@Param("pagePage") long pagePage, long pageSize, String beginTime, String endTime);


}
