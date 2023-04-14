package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.entity.IndexRule;
import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.IndexRuleMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
import cn.ldap.ldap.service.PortLinkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.ReusableMessage;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

@Service
@Slf4j
public class IndexRuleServiceImpl extends ServiceImpl<IndexRuleMapper, IndexRule> implements IndexRuleService {


    @Value("${filePath.runPath}")
    private String runPath;

    private static final String SERVICE = "Service";

    private static final String LDAPS_HEAD = "SLAPD_URLS=ldaps://0.0.0.0:";

    private static final String LDAP_HEAD = "SLAPD_URLS=ldap://0.0.0.0:";

    private static final String BEFORE_COMMAND = "SLAPD_URLS=ldaps://0.0.0.0:";

    private static final String AFTER_COMMAND = "/ ldapi://0.0.0.0:";

    private static final String DOUBLE_COMMAND = "/\" \"SLAPD_OPTIONS=";

    private static final String LAST_COMMAND = "SLAPD_OPTIONS=";

    private static final String RESTART_FAIL = "重启服务失败";

    private static final String RESTART_SUCCESS = "重启服务成功";

    private static final String SAFE_SERVER = "安全协议服务";

    private static final String STANDART_SERVER = "标准协议服务";

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

    private static final String START = "TLSVerifyClient";

    @Resource
    private LdapConfigServiceImpl ldapConfigService;

    @Resource
    private PortLinkService portLinkService;

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
     *
     * @param serverDto
     * @return
     */
    @Override
    public ResultVo<Object> sslOperation(ServerDto serverDto) {
        if ((BeanUtil.isEmpty(serverDto.getSafeOperation()) && BeanUtil.isEmpty(serverDto.getSafeOperation()))
                || (false == serverDto.getOperation() && false == serverDto.getSafeOperation())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_EMPTY);
        }
        //定义一个命令
        String command = "";
        ResultVo<Object> objectResultVo = null;
        //标准协议
        if (true == serverDto.getOperation() && false == serverDto.getSafeOperation()){
            command = LDAP_HEAD + serverDto.getPort() + LAST_COMMAND;
            objectResultVo = onlyOne(command, serverDto, STANDART_SERVER);
            log.info("标准协议配置为:{}", command);
        }
        //安全协议
        if (false == serverDto.getOperation() && true == serverDto.getSafeOperation()){
            command = LDAPS_HEAD + serverDto.getSafePort() + LAST_COMMAND;
            objectResultVo = onlyOne(command, serverDto, STANDART_SERVER);
            log.info("安全协议开启端口:{}", command);
        }
        //安全协议标准协议全部开启
        if (true == serverDto.getOperation() && serverDto.getSafeOperation()){
            command = LDAPS_HEAD + serverDto.getSafePort() + AFTER_COMMAND + serverDto.getPort() + DOUBLE_COMMAND;
            objectResultVo = twice(command, serverDto);
        }
        if(true == serverDto.getSafeOperation()) {
            syncConfig(serverDto);
        }

        return objectResultVo;
    }

    public ResultVo<Object> onlyOne(String command,ServerDto serverDto,String serverName){
        log.info("安全协议、标准协议全部开启:{}", command);
        //修改slapd.service 配置文件
        try {
            Wini wini = new Wini(new File(runPath));
            Profile.Section section = wini.get(SERVICE);
            section.put("Environment", command);
            wini.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean compare = true;
//        try {
//            Runtime.getRuntime().exec(RESTART_COMMAND, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Boolean result = ldapConfigService.linuxCommand(SERVER_NAME);

        Boolean result = true;
        QueryWrapper<PortLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PortLink::getServerName,serverName);
        PortLink portLink = portLinkService.getOne(queryWrapper);
        portLink.setPort(serverDto.getPort());
        portLink.setStatus(result);
        portLinkService.updateById(portLink);
        if (result == compare) {
            return ResultUtil.success(RESTART_SUCCESS);
        } else {
            return ResultUtil.fail(RESTART_FAIL);
        }
    }


    public ResultVo<Object> twice(String command,ServerDto serverDto){
        log.info("安全协议、标准协议全部开启:{}", command);
        //修改slapd.service 配置文件
        try {
            Wini wini = new Wini(new File(runPath));
            Profile.Section section = wini.get(SERVICE);
            section.put("Environment", command);
            wini.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean compare = true;
//        try {
//            Runtime.getRuntime().exec(RESTART_COMMAND, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Boolean result = ldapConfigService.linuxCommand(SERVER_NAME);
        Boolean result = true;
        List<PortLink> portLinkList = portLinkService.list();
        for (PortLink portLink : portLinkList) {
            if (STANDART_SERVER.equals(portLink.getServerName())){
                portLink.setStatus(result);
                portLink.setPort(serverDto.getPort());
            }else {
                portLink.setStatus(result);
                portLink.setPort(serverDto.getSafePort());
            }
        }
        portLinkService.updateBatchById(portLinkList);
        if (result == compare) {
            return ResultUtil.success(RESTART_SUCCESS);
        } else {
            return ResultUtil.fail(RESTART_FAIL);
        }
    }


    public void syncConfig(ServerDto serverDto) {
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SystemException(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String fileName = configPath;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.trim().startsWith(START)) {
                    break;
                }
                String oldData = lineStr;
                stringBuilder.append(oldData).append(FEED);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

    public String splicingConfigParam(StringBuilder stringBuilder, ServerDto serverDto) {
        //配置方式
        stringBuilder.append("TLSVerifyClient" + SPACE_DATA + serverDto.getSslAuthStrategy() + FEED);
        return stringBuilder.toString();
    }
}
