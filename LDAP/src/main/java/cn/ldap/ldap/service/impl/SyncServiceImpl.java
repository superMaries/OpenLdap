package cn.ldap.ldap.service.impl;

import cn.byzk.util.CryptUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.ldap.ldap.common.dto.FromSyncDto;
import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.util.IscSignUtil;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;

import static cn.ldap.ldap.common.enums.ExceptionEnum.*;

@Service
@Slf4j
public class SyncServiceImpl implements SyncService {

    @Value("${filePath.followCertPath}")
    private String followPath;

    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;




    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    /**
     * 换行
     */
    private static final String FEED = "\n";

    private static final String START = "syncprov-checkpoint";

    private static final String FIRST = "overlay syncprov";

    private static final String START_FROM = "syncrepl";

    private static final String RID = "rid=";

    private static final String SYNCREPL = "syncrepl";

    private static final String PROVIDER = "provider=";

    private static final String TYPE = "type=";

    private static final String REFRESH_AND_PERSIST = "refreshAndPersist";

    private static final String INTERVAL = "interval=";

    private static final String SEARCH_BASE = "searchbase=";

    private static final String FILTER = "filter=";

    private static final String OBJ = "(objectClass=*)";

    private static final String SCOPE = "scope=";

    private static final String SUB = "sub";

    private static final String SCHEMA_CHECK_ING = "schemachecking=";

    private static final String OFF = "off";

    private static final String BIND_METHOD = "bindmethod=";

    private static final String SIMPLE = "simple";

    private static final String BIND_DN = "binddn=";

    private static final String CREDENTIALS = "credentials=";

    private static final String RETRY = "retry=\"60 +\"";


    private static final String TLS_TYPE = "tls_reqcert=";

    private static final String NEVER = "never";

    private static final String DEMAND = "demand";

    private static final String CA_CERT = "tls_cacert=";

    private static final String CERT = "tls_cert=";

    private static final String KEY = "tls_key=";

    private static final String CA_CER = "ca.cer";

    private static final String SERVER_CER = "server.cer";

    private static final String SERVER_KEY = "server.key";


    /**
     * 主服务同步配置
     *
     * @param syncDto
     * @return
     */
    @Override
    public ResultVo<Object> syncConfig(SyncDto syncDto) {
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String fileName = configPath;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.trim().startsWith(START) || lineStr.trim().startsWith(FIRST)) {
                    break;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = splicingConfigParam(stringBuilder, syncDto);
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success();
    }

    /**
     * 从服务同步配置
     *
     * @param fromSyncDto
     * @return
     */
    @Override
    public ResultVo<Object> fromSyncConfig(FromSyncDto fromSyncDto) {

        StringBuilder stringBuilder = new StringBuilder();
        log.info("从服务配置入参：", fromSyncDto);
        if (ObjectUtils.isEmpty(fromSyncDto)
                || ObjectUtils.isEmpty(fromSyncDto.getSyncPoint())) {
            throw new SysException(ExceptionEnum.PARAM_ERROR);
        }

        //判断节点是否存在
//        LdapTemplate newLdapTemplate = certTreeService.fromPool();
//        if (LdapUtil.isExitRdn(newLdapTemplate, fromSyncDto.getSyncPoint())) {
//            throw new SysException(ExceptionEnum.NODE_NOT_EXIT);
//        }

        File file = new File(configPath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        String data = "";

        String fileName = configPath;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.trim().startsWith(START_FROM)) {
                    break;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!fromSyncDto.getIfSafe()){
            data = splicingConfigFrom(stringBuilder, fromSyncDto);
        }else {
            if (fromSyncDto.getIfBothWay()){
                if (ObjectUtils.isEmpty(fromSyncDto.getCaCer())) {
                    throw new SysException(CER_NULL_ERROR);
                }
                makeOneCer(fromSyncDto.getCaCer());
                data = splicingConfigFromSimple(stringBuilder, fromSyncDto);
            }else {
                if (ObjectUtils.isEmpty(fromSyncDto.getCaCer())) {
                    throw new SysException(CER_NULL_ERROR);
                }
                if (ObjectUtils.isEmpty(fromSyncDto.getServerCer())) {
                    throw new SysException(CER_NULL_ERROR);
                }
                if (ObjectUtils.isEmpty(fromSyncDto.getServerKey())) {
                    throw new SysException(KEY_NULL_ERROR);
                }
                String caCer = fromSyncDto.getCaCer();
                String serverCer = fromSyncDto.getServerCer();

                String caCert = IscSignUtil.otherToBase64(caCer);
                //  log.info("CA证书:{}",caCert);
                String serverCert = IscSignUtil.otherToBase64(serverCer);
                //   log.info("服务器证书:{}",serverCert);
                //验证书链
                log.info("处理后CA证书:{}", caCert);
                log.info("处理后服务器证书:{}", serverCert);
                boolean validateCertChain = CryptUtil.validateCertChain(serverCert, caCert);
                if (StaticValue.FALSE == validateCertChain) {
                    throw new SysException(ExceptionEnum.VALIDATE_ERROR);
                }

                makeCer(caCer, serverCer, fromSyncDto.getServerKey());
                data = splicingConfigFromBothWay(stringBuilder, fromSyncDto);
            }
        }
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success();
    }

//    @Override
//    public ResultVo<Object> fromSyncConfig(FromSyncDto fromSyncDto) {
//        log.info("从服务配置入参：", fromSyncDto);
//        if (ObjectUtils.isEmpty(fromSyncDto)
//                || ObjectUtils.isEmpty(fromSyncDto.getSyncPoint())) {
//            throw new SysException(ExceptionEnum.PARAM_ERROR);
//        }
//
//        //判断节点是否存在
//        LdapTemplate newLdapTemplate = certTreeService.fromPool();
//        if (LdapUtil.isExitRdn(newLdapTemplate, fromSyncDto.getSyncPoint())) {
//            throw new SysException(ExceptionEnum.NODE_NOT_EXIT);
//        }
//
//        File file = new File(configPath);
//        if (!file.exists()) {
//            throw new SysException(FILE_NOT_EXIST);
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        String fileName = configPath;
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
//            String lineStr = null;
//            while ((lineStr = bufferedReader.readLine()) != null) {
//                if (lineStr.trim().startsWith(START_FROM)) {
//                    break;
//                }
//                String oldData = lineStr;
//                stringBuilder.append(oldData).append(FEED);
//            }
//
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (!fromSyncDto.getMainServerUrl().startsWith("ldap://")){
//            return ResultUtil.fail(ExceptionEnum.LDAP_URL_ERROR);
//        }
//        String data = splicingConfigFrom(stringBuilder, fromSyncDto);
//        try {
//            //采用流的方式进行写入配置
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
//            bufferedWriter.write(data);
//            bufferedWriter.flush();
//            bufferedWriter.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return ResultUtil.success();
//    }

    /**
     * 主服务配置数据拼接
     *
     * @param stringBuilder
     * @param syncDto
     * @return
     */
    public String splicingConfigParam(StringBuilder stringBuilder, SyncDto syncDto) {
        //主服务配置文件
        stringBuilder.append(FIRST).append(FEED).append(START).append(SPACE_DATA).append(syncDto.getTriggerSyncMaxNum()).append(SPACE_DATA).append(syncDto.getSyncTimeInterval()).append(FEED);
        return stringBuilder.toString();
    }

    private static final String DATA_BASE = "database monitor";
    /**
     * 从服务配置数据拼接
     *
     * @param stringBuilder
     * @param fromSyncDto
     * @return
     */
    public String splicingConfigFrom(StringBuilder stringBuilder, FromSyncDto fromSyncDto) {
        //从服务配置文件
        stringBuilder.append(SYNCREPL).append(SPACE_DATA).append(RID).append(fromSyncDto.getRid()).append(FEED)
                .append(SPACE_DATA).append(PROVIDER).append(fromSyncDto.getMainServerUrl()).append(FEED)
                .append(SPACE_DATA).append(TYPE).append(REFRESH_AND_PERSIST).append(FEED)
                .append(SPACE_DATA).append(INTERVAL).append(fromSyncDto.getSyncTime()).append(FEED)
                .append(SPACE_DATA).append(SEARCH_BASE).append(fromSyncDto.getSyncPoint()).append(FEED)
                .append(SPACE_DATA).append(FILTER).append(OBJ).append(FEED)
                .append(SPACE_DATA).append(SCOPE).append(SUB).append(FEED)
                .append(SPACE_DATA).append(SCHEMA_CHECK_ING).append(OFF).append(FEED)
                .append(SPACE_DATA).append(BIND_METHOD).append(SIMPLE).append(FEED)
                .append(SPACE_DATA).append(BIND_DN).append(fromSyncDto.getMainServerAccount()).append(FEED)
                .append(SPACE_DATA).append(CREDENTIALS).append(fromSyncDto.getMainServerPassword()).append(FEED)
                .append(SPACE_DATA).append(RETRY).append(FEED)
                .append(DATA_BASE).append(FEED);
        return stringBuilder.toString();
    }


    public String splicingConfigFromSimple(StringBuilder stringBuilder, FromSyncDto fromSyncDto) {
        //从服务配置文件
        stringBuilder.append(SYNCREPL).append(SPACE_DATA).append(RID).append(fromSyncDto.getRid()).append(FEED)
                .append(SPACE_DATA).append(PROVIDER).append(fromSyncDto.getMainServerUrl()).append(FEED)
                .append(SPACE_DATA).append(TYPE).append(REFRESH_AND_PERSIST).append(FEED)
                .append(SPACE_DATA).append(INTERVAL).append(fromSyncDto.getSyncTime()).append(FEED)
                .append(SPACE_DATA).append(SEARCH_BASE).append(fromSyncDto.getSyncPoint()).append(FEED)
                .append(SPACE_DATA).append(FILTER).append(OBJ).append(FEED)
                .append(SPACE_DATA).append(SCOPE).append(SUB).append(FEED)
                .append(SPACE_DATA).append(SCHEMA_CHECK_ING).append(OFF).append(FEED)
                .append(SPACE_DATA).append(BIND_METHOD).append(SIMPLE).append(FEED)
                .append(SPACE_DATA).append(BIND_DN).append(fromSyncDto.getMainServerAccount()).append(FEED)
                .append(SPACE_DATA).append(CREDENTIALS).append(fromSyncDto.getMainServerPassword()).append(FEED)
                .append(SPACE_DATA).append(RETRY).append(FEED)
                .append(SPACE_DATA).append(TLS_TYPE).append(NEVER).append(FEED)
                .append(SPACE_DATA).append(CA_CERT).append(followPath).append(CA_CER).append(FEED)
                .append(DATA_BASE).append(FEED);

        return stringBuilder.toString();
    }

    public String splicingConfigFromBothWay(StringBuilder stringBuilder, FromSyncDto fromSyncDto) {
        //从服务配置文件
        stringBuilder.append(SYNCREPL).append(SPACE_DATA).append(RID).append(fromSyncDto.getRid()).append(FEED)
                .append(SPACE_DATA).append(PROVIDER).append(fromSyncDto.getMainServerUrl()).append(FEED)
                .append(SPACE_DATA).append(TYPE).append(REFRESH_AND_PERSIST).append(FEED)
                .append(SPACE_DATA).append(INTERVAL).append(fromSyncDto.getSyncTime()).append(FEED)
                .append(SPACE_DATA).append(SEARCH_BASE).append(fromSyncDto.getSyncPoint()).append(FEED)
                .append(SPACE_DATA).append(FILTER).append(OBJ).append(FEED)
                .append(SPACE_DATA).append(SCOPE).append(SUB).append(FEED)
                .append(SPACE_DATA).append(SCHEMA_CHECK_ING).append(OFF).append(FEED)
                .append(SPACE_DATA).append(BIND_METHOD).append(SIMPLE).append(FEED)
                .append(SPACE_DATA).append(BIND_DN).append(fromSyncDto.getMainServerAccount()).append(FEED)
                .append(SPACE_DATA).append(CREDENTIALS).append(fromSyncDto.getMainServerPassword()).append(FEED)
                .append(SPACE_DATA).append(RETRY).append(FEED)
                .append(SPACE_DATA).append(TLS_TYPE).append(DEMAND).append(FEED)
                .append(SPACE_DATA).append(CA_CERT).append(followPath).append(CA_CER).append(FEED)
                .append(SPACE_DATA).append(CERT).append(followPath).append(SERVER_CER).append(FEED)
                .append(SPACE_DATA).append(KEY).append(followPath).append(SERVER_KEY).append(FEED)
                .append(DATA_BASE).append(FEED);
        return stringBuilder.toString();
    }


    public void makeCer(String caCer, String serverCer, String key) {
        caCer = IscSignUtil.otherToBase64(caCer);
        serverCer = IscSignUtil.otherToBase64(serverCer);

        writeCer(caCer, CA_CER);
        writeCer(serverCer, SERVER_CER);
        writekey(key, SERVER_KEY);
    }

    public void makeOneCer(String caCer) {
        String caCerStr = IscSignUtil.otherToBase64(caCer);
        writeCer(caCerStr, CA_CER);

    }

    public void writeCer(String data, String name) {
        String beginCertificate = StaticValue.BEGIN_CERTIFICATE;
        String endCertificate = StaticValue.END_CERTIFICATE;
        String filePath = followPath + name;
        String writeData = beginCertificate + StaticValue.N + data + StaticValue.N + endCertificate;
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(writeData);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writekey(String data, String name) {
        String beginKey = "-----BEGIN RSA PRIVATE KEY-----";
        String entKey = "-----END RSA PRIVATE KEY-----";
        String serverKey = "";
        byte[] decode = Base64Decoder.decode(data.getBytes());
        String decStr = new String(decode);
        boolean isBase64 = Base64.isBase64(decStr);
        if (isBase64) {
            serverKey = decStr;
        } else {
            serverKey = beginKey + StaticValue.N + data + StaticValue.N + entKey;

        }

        String filePath = followPath + name;

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(serverKey);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
