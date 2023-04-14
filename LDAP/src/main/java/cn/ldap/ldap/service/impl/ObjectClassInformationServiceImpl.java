package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.vo.ObjectDataDto;
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


    @Override
    public List<ObjectDataDto> queryObjectAndAttribute() {

        LDAPConnection ldapConnection = null;
        //和LDAP服务进行连接
        try {
            ldapConnection = new LDAPConnection(url, port, userName, password);
        } catch (LDAPException e) {
            e.printStackTrace();
        }
        // 获取 LDAP 服务器的 schema 信息
        Schema schema = null;
        try {
            schema = ldapConnection.getSchema();
        } catch (LDAPException e) {
            e.printStackTrace();
        }
        // 获取所有 objectClass
        Set<ObjectClassDefinition> objectClasses = schema.getObjectClasses();
        List<ObjectDataDto> dtoList = new ArrayList<>();

        // 遍历所有 objectClass
        for (ObjectClassDefinition objectClass : objectClasses) {
            ObjectDataDto objectDataDto = new ObjectDataDto();
            log.info("ObjectClass:{}" + objectClass.getNameOrOID());
            objectDataDto.setObjectClassName(objectClass.getNameOrOID());
            List<String> requireAttribute = Arrays.asList(objectClass.getRequiredAttributes());
            List<String> optionalAttribute = Arrays.asList(objectClass.getOptionalAttributes());
            List<String> resultList = new ArrayList<>();
            //整合属性
            if (!CollectionUtils.isEmpty(requireAttribute)) {
                resultList.addAll(requireAttribute);
            }
            if (!CollectionUtils.isEmpty(optionalAttribute)) {
                resultList.addAll(optionalAttribute);
            }
            //存入到类中
            objectDataDto.setAttributesName(resultList);
            for (String attribute : objectClass.getRequiredAttributes()) {
                log.info("Attributes:{}", attribute);
            }
            for (String attribute : objectClass.getOptionalAttributes()) {
                log.info("Attributes:{}", attribute);
            }
            dtoList.add(objectDataDto);
        }
        ldapConnection.close();
        return dtoList;
    }
}
