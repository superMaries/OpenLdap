package cn.ldap.ldap.service.impl;

import cn.byzk.util.CryptUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.entity.IndexRule;
import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.entity.SSLConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.IndexRuleMapper;
import cn.ldap.ldap.common.util.IscSignUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
import cn.ldap.ldap.service.PortLinkService;
import cn.ldap.ldap.service.SSLConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.omg.CORBA.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class IndexRuleServiceImpl extends ServiceImpl<IndexRuleMapper, IndexRule> implements IndexRuleService {


    @Value("${filePath.slapdPath}")
    private String slapdPath;


    @Value("${filePath.configPath}")
    private String configPath;


    private static final String CERT_Start_DROP = "---";

    private static final String SPACE = " ";

    private static final String LDAPS_HEAD = "\"ldaps://0.0.0.0:";

    private static final String YIN = "\"";

    private static final String AFTER_COMMAND = "\"ldap://0.0.0.0:";

    private static final String UNDER_COMMAND = "ldap://0.0.0.0:";


    private static final String BEHIND = "nohup ";

    private static final String HH = " -h ";

    private static final String LAST_COMMAND = "-f \"";

    private static final String THE_END = "\" > /dev/null 2>&1 &";

    private static final String RESTART_FAIL = "重启服务失败";

    private static final String RESTART_SUCCESS = "重启服务成功";


    private static final String STANDART_SERVER = "标准协议服务";

    private static final String SAFE_SERVER = "安全协议服务";




    /**
     * 配置文件所在路径
     */
    @Value("${filePath.shPath}")
    private String shPath;

    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    /**
     * 换行
     */
    private static final String FEED = "\n";

    private static final String START = "TLSVerifyClient";

    private static final String ENVIRONMENT = "Environment";

    private static final String CASERVER_CERT = "ca.cer";

    private static final String SERVER_CERT = "server.cer";

    private static final String SERVER_KEY = "server.key";

    private static final String CA_CERT = "TLSCACertificateFile";

    private static final String SER_CERT = "TLSCertificateFile";

    private static final String SER_KEY = "TLSCertificateKeyFile";

    @Resource
    private PortLinkService portLinkService;

    @Resource
    private SSLConfigService sslConfigService;

    //证书路径
    @Value("${filePath.certPath}")
    private String certPath;

    /**
     * 查询索引规则
     *
     * @return
     */
    @Override
    public ResultVo<List<String>> queryIndexRule() {
        List<IndexRule> indexRuleList = this.list();
        if (CollectionUtils.isEmpty(indexRuleList)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        }
        return ResultUtil.success(indexRuleList);
    }

    /**
     * 开启或关闭SSL
     * _
     *
     * @param serverDto
     * @return
     */
    @Override
    public ResultVo<Object> sslOperation(ServerDto serverDto) {
        if ((BeanUtil.isEmpty(serverDto.getSafeOperation()) && BeanUtil.isEmpty(serverDto.getSafeOperation()))
                || (!serverDto.getOperation() && !serverDto.getSafeOperation())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_EMPTY);
        }

        //数据库存储数据
        SSLConfig sslConfig = sslConfigService.getOne(null);

        List<PortLink> list = new ArrayList<>();

        if (ObjectUtils.isEmpty(sslConfig)) {
            SSLConfig sslConfigNew = new SSLConfig();
            PortLink portLinkCommon = new PortLink();

            PortLink portLinkSafe = new PortLink();

            sslConfigNew.setCaName(certPath + CASERVER_CERT);
            sslConfigNew.setServerName(certPath + SERVER_CERT);
            sslConfigNew.setKeyName(certPath + SERVER_KEY);

            if (!BeanUtil.isEmpty(serverDto.getOperation())) {
                sslConfigNew.setOperation(serverDto.getOperation());
                portLinkCommon.setStatus(serverDto.getOperation().toString());
            }
            if (!BeanUtil.isEmpty(serverDto.getPort())) {
                sslConfigNew.setPort(serverDto.getPort());
                portLinkCommon.setPort(serverDto.getPort());
                portLinkCommon.setServerName("标准协议服务");
            }
            if (!BeanUtil.isEmpty(serverDto.getSafeOperation())) {
                sslConfigNew.setSafeOperation(serverDto.getSafeOperation());
                portLinkSafe.setStatus(serverDto.getSafeOperation().toString());
            }
            if (!BeanUtil.isEmpty(serverDto.getSafePort())) {
                sslConfigNew.setSafePort(serverDto.getSafePort());
                portLinkSafe.setPort(serverDto.getSafePort());
                portLinkSafe.setServerName("安全协议服务");
            }
            if (!BeanUtil.isEmpty(serverDto.getSslAuthStrategy())) {
                sslConfigNew.setSslAuthStrategy(serverDto.getSslAuthStrategy());
            }
            list.add(portLinkCommon);
            list.add(portLinkSafe);
            portLinkService.saveBatch(list);
            sslConfigService.save(sslConfigNew);
        } else {
            sslConfig.setCaName(certPath + CASERVER_CERT);
            sslConfig.setServerName(certPath + SERVER_CERT);
            sslConfig.setKeyName(certPath + SERVER_KEY);
            if (!BeanUtil.isEmpty(serverDto.getOperation())) {
                sslConfig.setOperation(serverDto.getOperation());
            }
            if (!BeanUtil.isEmpty(serverDto.getPort())) {
                sslConfig.setPort(serverDto.getPort());
            }
            if (!BeanUtil.isEmpty(serverDto.getSafeOperation())) {
                sslConfig.setSafeOperation(serverDto.getSafeOperation());
            }
            if (!BeanUtil.isEmpty(serverDto.getSafePort())) {
                sslConfig.setSafePort(serverDto.getSafePort());
            }
            if (!BeanUtil.isEmpty(serverDto.getSslAuthStrategy())) {
                sslConfig.setSslAuthStrategy(serverDto.getSslAuthStrategy());
            }
            sslConfigService.updateById(sslConfig);
        }


        //定义一个命令
        String command = "";
        ResultVo<Object> objectResultVo = null;
        //标准协议
        if (serverDto.getOperation() && !serverDto.getSafeOperation()) {
            if (serverDto.getPort().equals(serverDto.getSafePort())) {
                return ResultUtil.fail(ExceptionEnum.LDAP_PORT_ERROR);
            }
            //标准协议命令
            command = BEHIND +slapdPath + HH + SPACE + AFTER_COMMAND + serverDto.getPort() + YIN + SPACE + LAST_COMMAND + configPath + THE_END;
            updateOther(SAFE_SERVER,serverDto.getSafePort());
            objectResultVo = onlyOne(command, serverDto, STANDART_SERVER);
            log.info("标准协议配置为:{}", command);
        }
        //安全协议
        if (!serverDto.getOperation() && serverDto.getSafeOperation()) {
            if (serverDto.getPort().equals(serverDto.getSafePort())) {
                return ResultUtil.fail(ExceptionEnum.LDAP_PORT_ERROR);
            }
            if (ObjectUtils.isEmpty(serverDto.getCaCer()) || ObjectUtils.isEmpty(serverDto.getServerCer()) || ObjectUtils.isEmpty(serverDto.getKey())) {
                return ResultUtil.fail(ExceptionEnum.CER_ERROR);
            }
            try {
                validCert(serverDto.getServerCer(), serverDto.getCaCer());
            } catch (SystemException systemException) {
                return ResultUtil.fail(ExceptionEnum.VALIDATE_ERROR);
            }

            makeCer(serverDto.getCaCer(), serverDto.getServerCer(), serverDto.getKey());
            //安全协议命令
            command = BEHIND +slapdPath + HH + SPACE + LDAPS_HEAD + serverDto.getSafePort() + YIN + SPACE + LAST_COMMAND + configPath + THE_END;
            updateOther(STANDART_SERVER,serverDto.getPort());
            objectResultVo = onlyOne(command, serverDto, SAFE_SERVER);
            log.info("安全协议开启端口:{}", command);
        }
        //安全协议标准协议全部开启
        if (serverDto.getOperation() && serverDto.getSafeOperation()) {
            if (serverDto.getPort().equals(serverDto.getSafePort())) {
                return ResultUtil.fail(ExceptionEnum.LDAP_PORT_ERROR);
            }

            if (ObjectUtils.isEmpty(serverDto.getCaCer()) || ObjectUtils.isEmpty(serverDto.getServerCer()) || ObjectUtils.isEmpty(serverDto.getKey())) {
                return ResultUtil.fail(ExceptionEnum.CER_ERROR);
            }
            try {
                validCert(serverDto.getServerCer(), serverDto.getCaCer());
            } catch (SystemException systemException) {
                return ResultUtil.fail(ExceptionEnum.VALIDATE_ERROR);
            }

            makeCer(serverDto.getCaCer(), serverDto.getServerCer(), serverDto.getKey());
            //双重协议命令
            command = BEHIND +slapdPath + HH + SPACE + LDAPS_HEAD + serverDto.getSafePort() + SPACE + UNDER_COMMAND + serverDto.getPort() + YIN + SPACE + LAST_COMMAND + configPath + THE_END;
            objectResultVo = twice(command, serverDto);
        }
        //保存或者修改
        sslConfigService.saveOrUpdate(sslConfig);
        if (serverDto.getSafeOperation()) {
            syncConfig(serverDto);
        } else {
            deleteTLS();
        }

        return objectResultVo;
    }

    public ResultVo<Object> validCert(String caCer, String serverCer) {

        String caCert = IscSignUtil.otherToBase64(caCer);
        //  log.info("CA证书:{}",caCert);
        String serverCert = IscSignUtil.otherToBase64(serverCer);
        //   log.info("服务器证书:{}",serverCert);
        //验证书链
        log.info("处理后CA证书:{}", caCert);
        log.info("处理后服务器证书:{}", serverCert);
        boolean validateCertChain = CryptUtil.validateCertChain(caCert, serverCert);
        if (StaticValue.FALSE == validateCertChain) {
            throw new SysException(ExceptionEnum.VALIDATE_ERROR);
        }
        return null;
    }


    public void updateOther(String serverName,String port) {//修改数据库操作
        Boolean status = false;
        QueryWrapper<PortLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PortLink::getServerName, serverName);
        PortLink portLink = portLinkService.getOne(queryWrapper);

        if (!ObjectUtils.isEmpty(portLink)) {
            portLink.setStatus(status.toString());
            portLink.setPort(port);
        }

        portLinkService.updateById(portLink);
    }

    /**
     * 开启单协议方法
     *
     * @param command
     * @param serverDto
     * @param serverName
     * @return
     */
    public ResultVo<Object> onlyOne(String command, ServerDto serverDto, String serverName) {
        log.info("安全协议、标准协议全部开启:{}", command);
        //修改sh文件
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(shPath))) {

            bufferedWriter.write(command);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean result = true;
        //修改数据库操作
        QueryWrapper<PortLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PortLink::getServerName, serverName);
        PortLink portLink = portLinkService.getOne(queryWrapper);
        if (serverName.equals(STANDART_SERVER)) {
            portLink.setPort(serverDto.getPort());
        } else {
            portLink.setPort(serverDto.getSafePort());
        }
        portLink.setStatus(result.toString());
        portLinkService.updateById(portLink);
        if (result) {
            return ResultUtil.success(RESTART_SUCCESS);
        } else {
            return ResultUtil.fail(RESTART_FAIL);
        }
    }

    /**
     * 开启双协议方法
     *
     * @param command
     * @param serverDto
     * @return
     */
    public ResultVo<Object> twice(String command, ServerDto serverDto) {
        log.info("安全协议、标准协议全部开启:{}", command);
        //修改sh文件
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(shPath))) {
            bufferedWriter.write(command);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean result = true;
        List<PortLink> portLinkList = portLinkService.list();
        //修改数据库
        for (PortLink portLink : portLinkList) {
            if (STANDART_SERVER.equals(portLink.getServerName())) {
                portLink.setStatus(result.toString());
                portLink.setPort(serverDto.getPort());
            } else {
                portLink.setStatus(result.toString());
                portLink.setPort(serverDto.getSafePort());
            }
        }
        portLinkService.updateBatchById(portLinkList);
        //判断状态
        if (result) {
            return ResultUtil.success();
        } else {
            return ResultUtil.fail();
        }
    }

    /**
     * 主服务同步配置
     *
     * @param serverDto
     */

    public void syncConfig(ServerDto serverDto) {
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SysException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String fileName = configPath;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));) {
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.trim().startsWith(START)) {
                    break;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = splicingConfigParam(stringBuilder, serverDto);
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void
    deleteTLS() {
        try {
            // 读取文件
            File inputFile = new File(configPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // 写入文件
            File outputFile = new File("output.conf");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // 逐行读取并处理
            String line;
            while ((line = reader.readLine()) != null) {
                // 判断是否以TLS开头
                if (!line.startsWith("TLS")) {
                    // 写入到输出文件
                    writer.write(line + "\n");
                }
            }

            // 关闭文件流
            reader.close();
            writer.close();

            // 删除原文件
            inputFile.delete();

            // 重命名输出文件
            outputFile.renameTo(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 封装证书
     */
    public void makeCer(String caCer, String serverCer, String key) {
        caCer = IscSignUtil.otherToBase64(caCer);
        serverCer = IscSignUtil.otherToBase64(serverCer);

        writeCer(caCer, CASERVER_CERT);
        writeCer(serverCer, SERVER_CERT);
        writekey(key, SERVER_KEY);
    }

    public void writeCer(String data, String name) {
        String beginCertificate = StaticValue.BEGIN_CERTIFICATE;
        String endCertificate = StaticValue.END_CERTIFICATE;
        String filePath = certPath + name;
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

        String filePath = certPath + name;

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(serverKey);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 配置拼接
     *
     * @param stringBuilder
     * @param serverDto
     * @return
     */
    public String splicingConfigParam(StringBuilder stringBuilder, ServerDto serverDto) {
        //配置方式
        stringBuilder.append(START).append(SPACE_DATA).append(serverDto.getSslAuthStrategy()).append(FEED)
                .append(CA_CERT).append(SPACE).append(certPath).append(CASERVER_CERT).append(FEED)
                .append(SER_CERT).append(SPACE).append(certPath).append(SERVER_CERT).append(FEED)
                .append(SER_KEY).append(SPACE).append(certPath).append(SERVER_KEY).append(FEED);
        return stringBuilder.toString();
    }
}
