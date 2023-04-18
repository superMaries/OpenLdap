package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ObjectDataDto;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.ObjectClassInformationService;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.ObjectClassDefinition;
import com.unboundid.ldap.sdk.schema.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Slf4j
@Service
public class ObjectClassInformationServiceImpl implements ObjectClassInformationService {

    @Value("${ldapLink.url}")
    private String url;
    @Value("${ldapLink.port}")
    private Integer port;
    @Value("${ldapLink.userName}")
    private String userName;
    @Value("${ldapLink.password}")
    private String password;


    /**
     * 查询属性和objectClass
     *
     * @return ObjectClass和属性的结构集合
     */
    @Override
    public ResultVo<List<ObjectDataDto>> queryObjectAndAttribute() {

        LDAPConnection ldapConnection = null;
        //和LDAP服务进行连接
        try {
            ldapConnection = new LDAPConnection(url, port, userName, password);
            log.info("连接URL:{}", url);
        } catch (LDAPException e) {
            log.error("连接异常:{}", e);
            return ResultUtil.fail(ExceptionEnum.LINK_ERROR);
        }
        // 获取 LDAP 服务器的 schema 信息
        Schema schema = null;
        try {
            schema = ldapConnection.getSchema();
        } catch (LDAPException e) {
            log.error("SCHEMA数据异常:{}", e);
            return ResultUtil.fail(ExceptionEnum.SCHEMA_ERROR);
        }finally {
            ldapConnection.close();
        }
        // 获取所有 objectClass
        Set<ObjectClassDefinition> objectClasses = schema.getObjectClasses();
        //定义结果集合
        List<ObjectDataDto> resultList = new ArrayList<>();

        // 遍历所有 objectClass
        ObjectDataDto objectDataDto = new ObjectDataDto();
        for (ObjectClassDefinition objectClass : objectClasses) {
            log.info("ObjectClass:{}", objectClass.getNameOrOID());
            //获取所有ObjectClass类
            objectDataDto.setObjectClassName(objectClass.getNameOrOID());
            //获取两种属性集合
            List<String> requireAttribute = Arrays.asList(objectClass.getRequiredAttributes());
            List<String> optionalAttribute = Arrays.asList(objectClass.getOptionalAttributes());
            List<String> attributeList = new ArrayList<>();
            //整合属性（合并）
            if (!CollectionUtils.isEmpty(requireAttribute)) {
                attributeList.addAll(requireAttribute);
            }
            if (!CollectionUtils.isEmpty(optionalAttribute)) {
                attributeList.addAll(optionalAttribute);
            }
            //存入到类中
            objectDataDto.setAttributesName(attributeList);
            for (String attribute : objectClass.getRequiredAttributes()) {
                log.info("Attributes:{}", attribute);
            }
            for (String attribute : objectClass.getOptionalAttributes()) {
                log.info("Attributes:{}", attribute);
            }
            resultList.add(objectDataDto);
        }
        return ResultUtil.success(resultList);
    }
}
