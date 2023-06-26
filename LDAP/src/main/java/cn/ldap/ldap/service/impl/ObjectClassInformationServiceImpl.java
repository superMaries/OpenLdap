package cn.ldap.ldap.service.impl;

import byzk.sdk.SM4Util;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.ldap.ldap.common.dto.AttributeDto;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ObjectDataDto;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.ObjectClassInformationService;
import cn.ldap.ldap.util.decUtil;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.schema.ObjectClassDefinition;
import com.unboundid.ldap.sdk.schema.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Slf4j
@Service
public class ObjectClassInformationServiceImpl implements ObjectClassInformationService {

    @Value("${ldap.url}")
    private String url;

    @Value("${ldap.userDn}")
    private String userName;
    @Value("${ldap.password}")
    private String password;

    @Value("${objectClassConfig.open}")
    private String objConfig;


    /**
     * 查询属性和objectClass
     *
     * @return ObjectClass和属性的结构集合
     */
    @Override
    public ResultVo<List<ObjectDataDto>> queryObjectAndAttribute() {

        String connectionUrl = "";
        Integer connectionPort = 0;


        try {
            LDAPURL ldapurl = new LDAPURL(url);
            connectionUrl = ldapurl.getHost();
            connectionPort = ldapurl.getPort();
        } catch (LDAPException e) {
            throw new RuntimeException(e);
        }
        String secret = SM4Util.sm4DeData(password);

        LDAPConnection ldapConnection = null;



        //和LDAP服务进行连接
        try {
            ldapConnection = new LDAPConnection(connectionUrl, connectionPort, userName, secret);
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
        } finally {
            ldapConnection.close();
        }
        // 获取所有 objectClass
        Set<ObjectClassDefinition> objectClasses = schema.getObjectClasses();
        //定义结果集合
        List<ObjectDataDto> resultList = new ArrayList<>();
        //根据配置返回所有objectclass还是一个
        log.info("是否展示全部objectClass;{}", objConfig);
        if (objConfig.equals("all")) {
            // 遍历所有 objectClass

            for (ObjectClassDefinition objectClass : objectClasses) {
                ObjectDataDto objectDataDto = new ObjectDataDto();
                log.info("ObjectClass:{}", objectClass.getNameOrOID());
                //获取所有ObjectClass类
                //当获取的objectclass的名称为top，则需要过滤掉
                if (objectClass.getNameOrOID().equals("top")) {
                    continue;
                }
                objectDataDto.setObjectClassName(objectClass.getNameOrOID());
                //获取两种属性集合
                List<AttributeDto> result = new ArrayList<>();
                List<String> requireAttribute = Arrays.asList(objectClass.getRequiredAttributes());
                for (String name : requireAttribute) {
                    AttributeDto attributeDto = new AttributeDto();
                    attributeDto.setName(name);
                    attributeDto.setMust(true);
                    result.add(attributeDto);
                }
                List<String> optionalAttribute = Arrays.asList(objectClass.getOptionalAttributes());
                for (String name : optionalAttribute) {
                    AttributeDto attributeDto = new AttributeDto();
                    attributeDto.setName(name);
                    attributeDto.setMust(false);
                    result.add(attributeDto);
                }
                //存入到类中
                objectDataDto.setAttributesName(result);

                for (String attribute : objectClass.getRequiredAttributes()) {
                    log.info("Attributes:{}", attribute);
                }
                for (String attribute : objectClass.getOptionalAttributes()) {
                    log.info("Attributes:{}", attribute);
                }
                resultList.add(objectDataDto);
            }
        } else {
            for (ObjectClassDefinition objectClass : objectClasses) {
                ObjectDataDto objectDataDto = new ObjectDataDto();
                if ((null != objectClass && objectClass.getNameOrOID().equals("gscertinfo")) ||
                        null != objectClass && objectClass.getNameOrOID().equals("person")||
                        null != objectClass && objectClass.getNameOrOID().equals("organization")||
                        null != objectClass && objectClass.getNameOrOID().equals("organizationalUnit")||
                        null != objectClass && objectClass.getNameOrOID().equals("cRLDistributionPoint")) {
                    objectDataDto.setObjectClassName(objectClass.getNameOrOID());
                    List<AttributeDto> result = new ArrayList<>();
                    List<String> requireAttribute = Arrays.asList(objectClass.getRequiredAttributes());
                    for (String name : requireAttribute) {
                        AttributeDto attributeDto = new AttributeDto();
                        attributeDto.setName(name);
                        attributeDto.setMust(true);
                        result.add(attributeDto);
                    }
                    List<String> optionalAttribute = Arrays.asList(objectClass.getOptionalAttributes());
                    for (String name : optionalAttribute) {
                        AttributeDto attributeDto = new AttributeDto();
                        attributeDto.setName(name);
                        attributeDto.setMust(false);
                        result.add(attributeDto);
                    }
                    objectDataDto.setAttributesName(result);
                    resultList.add(objectDataDto);
                }
            }
        }
        return ResultUtil.success(resultList);
    }

    /**
     * 根据objectClassName查询属性
     *
     * @param objectClassName
     * @return 属性列表
     */
    @Override
    public ResultVo<Object> queryAttribute(String objectClassName) {


        String connectionUrl = "";
        Integer connectionPort = 0;
        try {
            LDAPURL ldapurl = new LDAPURL(url);
            connectionUrl = ldapurl.getHost();
            connectionPort = ldapurl.getPort();

        } catch (LDAPException e) {
            throw new RuntimeException(e);
        }


        LDAPConnection ldapConnection = null;
        String secret = SM4Util.sm4DeData(password);


        //和LDAP服务进行连接
        try {
            ldapConnection = new LDAPConnection(connectionUrl, connectionPort, userName, secret);
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
        } finally {
            ldapConnection.close();
        }
        // 获取所有 objectClass
        Set<ObjectClassDefinition> objectClasses = schema.getObjectClasses();
        //定义结果集合
        List<String> attributeList = new ArrayList<>();
        for (ObjectClassDefinition objectClass : objectClasses) {
            //获取所有ObjectClass类
            if (objectClassName.contains(objectClass.getNameOrOID())) {
                //获取两种属性集合
                List<String> requireAttribute = Arrays.asList(objectClass.getRequiredAttributes());
                List<String> optionalAttribute = Arrays.asList(objectClass.getOptionalAttributes());
                //整合属性（合并）
                if (!CollectionUtils.isEmpty(requireAttribute)) {
                    attributeList.addAll(requireAttribute);
                }
                if (!CollectionUtils.isEmpty(optionalAttribute)) {
                    attributeList.addAll(optionalAttribute);
                }
            }
        }
        return ResultUtil.success(attributeList);
    }


}

