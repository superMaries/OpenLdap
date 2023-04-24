package cn.ldap.ldap.service.impl;

import cn.byzk.util.CryptUtil;
import cn.hutool.core.bean.BeanUtil;
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
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class IndexRuleServiceImpl extends ServiceImpl<IndexRuleMapper, IndexRule> implements IndexRuleService {


    @Value("${filePath.runPath}")
    private String runPath;

    private static final String SERVICE = "Service";


    private static final String CERT_Start_DROP = "---";

    private static final String SPACE = " ";

    private static final String LDAPS_HEAD = "ldaps://0.0.0.0:";

    private static final String AFTER_COMMAND = "/ ldapi://0.0.0.0:";


    private static final String BEHIND = "nohup /usr/local/openldap/libexec/slapd -h";

    private static final String LAST_COMMAND = "-f /usr/local/openldap/etc/openldap/slapd.conf > /dev/null 2>&1 &";

    private static final String RESTART_FAIL = "重启服务失败";

    private static final String RESTART_SUCCESS = "重启服务成功";


    private static final String STANDART_SERVER = "标准协议服务";

    private static final String SAFE_SERVER = "安全协议服务";

    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;

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

    private static final String CASERVER_CERT = "ca.cert";

    private static final String SERVER_CERT = "server.cert";

    private static final String SERVER_KEY = "server.key";

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
        SSLConfig sslConfig = sslConfigService.getOne(null);
        if (ObjectUtils.isEmpty(sslConfig)) {
            SSLConfig sslConfigNew = new SSLConfig();
            if (!BeanUtil.isEmpty(serverDto.getOperation())) {
                sslConfigNew.setOperation(serverDto.getOperation());
            }
            if (!BeanUtil.isEmpty(serverDto.getPort())) {
                sslConfigNew.setPort(serverDto.getPort());
            }
            if (!BeanUtil.isEmpty(serverDto.getSafeOperation())) {
                sslConfigNew.setSafeOperation(serverDto.getSafeOperation());
            }
            if (!BeanUtil.isEmpty(serverDto.getSafePort())) {
                sslConfigNew.setSafePort(serverDto.getSafePort());
            }
            if (!BeanUtil.isEmpty(serverDto.getSslAuthStrategy())) {
                sslConfigNew.setSslAuthStrategy(serverDto.getSslAuthStrategy());
            }
            sslConfigNew.setKeyName(SERVER_KEY);
            sslConfigNew.setCaName(CASERVER_CERT);
            sslConfigNew.setServerName(SERVER_CERT);
            sslConfigService.save(sslConfigNew);
        } else {
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
            sslConfig.setKeyName(SERVER_KEY);
            sslConfig.setCaName(CASERVER_CERT);
            sslConfig.setServerName(SERVER_CERT);
            sslConfigService.updateById(sslConfig);
        }


        //CA证书
        String caCert = "";
        String certLine = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(certPath + CASERVER_CERT))) {
            while (null != (certLine=bufferedReader.readLine())) {

                if (null !=certLine &&!certLine.startsWith(CERT_Start_DROP) ) {
                    caCert += certLine;
                }
            }
        } catch (IOException e) {
            log.error("文件流错误:{}", e.getMessage());
            return ResultUtil.fail(ExceptionEnum.FILE_IO_ERROR);
        }
        //服务器证书
        String serverCert = "";
        String line = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(certPath + SERVER_CERT))) {

            while (null !=(line= bufferedReader.readLine())) {

                if (null != line &&!line.startsWith(CERT_Start_DROP)) {
                    serverCert += line;
                }
            }
        } catch (IOException e) {
            log.error("文件流错误:{}", e.getMessage());
            return ResultUtil.fail(ExceptionEnum.FILE_IO_ERROR);
        }
        log.info("处理前CA证书:{}",caCert);
        log.info("处理前服务器证书:{}",serverCert);
        caCert = IscSignUtil.otherToBase64(caCert);
        log.info("CA证书:{}",caCert);
        serverCert = IscSignUtil.otherToBase64(serverCert);
        log.info("服务器证书:{}",serverCert);
        //验证书链
        boolean validateCertChain = CryptUtil.validateCertChain(serverCert, caCert);
        if (StaticValue.FALSE == validateCertChain) {
            return ResultUtil.fail(ExceptionEnum.VALIDATE_ERROR);
        }
        //定义一个命令
        String command = "";
        ResultVo<Object> objectResultVo = null;
        //标准协议
        if (serverDto.getOperation() && !serverDto.getSafeOperation()) {
            //标准协议命令
            command = BEHIND + SPACE + serverDto.getPort() + SPACE + LAST_COMMAND;
            updateOther(SAFE_SERVER);
            objectResultVo = onlyOne(command, serverDto, STANDART_SERVER);
            log.info("标准协议配置为:{}", command);
        }
        //安全协议
        if (!serverDto.getOperation() && serverDto.getSafeOperation()) {
            //安全协议命令
            command = BEHIND + SPACE + LDAPS_HEAD + serverDto.getSafePort() + SPACE + LAST_COMMAND;
            updateOther(STANDART_SERVER);
            objectResultVo = onlyOne(command, serverDto, SAFE_SERVER);
            log.info("安全协议开启端口:{}", command);
        }
        //安全协议标准协议全部开启
        if (serverDto.getOperation() && serverDto.getSafeOperation()) {
            //双重协议命令
            command = BEHIND + SPACE + LDAPS_HEAD + serverDto.getSafePort() + SPACE + AFTER_COMMAND + serverDto.getPort() + SPACE + LAST_COMMAND;
            objectResultVo = twice(command, serverDto);
        }


        if (serverDto.getSafeOperation()) {
            syncConfig(serverDto);
        }

        return objectResultVo;
    }



    public void updateOther(String serverName){//修改数据库操作
        Boolean status = false;
        QueryWrapper<PortLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PortLink::getServerName, serverName);
        PortLink portLink = portLinkService.getOne(queryWrapper);
        portLink.setStatus(status.toString());
        portLinkService.updateById(portLink);}
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
        if (serverName.equals(STANDART_SERVER)){
            portLink.setPort(serverDto.getPort());
        }else {
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

    /**
     * 配置拼接
     *
     * @param stringBuilder
     * @param serverDto
     * @return
     */
    public String splicingConfigParam(StringBuilder stringBuilder, ServerDto serverDto) {
        //配置方式
        stringBuilder.append(START).append(SPACE_DATA).append(serverDto.getSslAuthStrategy()).append(FEED);
        return stringBuilder.toString();
    }
}
