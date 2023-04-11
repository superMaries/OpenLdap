package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.ldap.ldap.common.dto.ServerDto;
import cn.ldap.ldap.common.entity.IndexRule;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.IndexRuleMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.IndexRuleService;
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

@Service
@Slf4j
public class IndexRuleServiceImpl extends ServiceImpl<IndexRuleMapper, IndexRule> implements IndexRuleService {


    @Value("${filePath.runPath}")
    private String runPath;

    private static final String SERVICE = "Service";

    private static final String BEFORES_COMMAND = "SLAPD_URLS=ldaps://0.0.0.0:";

    private static final String BEFORE_COMMAND = "SLAPD_URLS=ldaps://0.0.0.0:";

    private static final String AFTER_COMMAND = "/ ldapi://0.0.0.0:389/\" \"SLAPD_OPTIONS=";

    private static final String RESTART_COMMAND = "systemctl restart slapd.service";

    private static final String SERVER_NAME = "slapd";

    private static final String RESTART_FAIL = "重启服务失败";

    private static final String RESTART_SUCCESS = "重启服务成功";

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
        if (ObjectUtils.isEmpty(serverDto) || BeanUtil.isEmpty(serverDto.getPort())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_EMPTY);
        }
        String command = "";
        if (serverDto.getOpenOrClose() == Boolean.TRUE.booleanValue()) {
            command = BEFORE_COMMAND+serverDto.getPort()+AFTER_COMMAND;
        } else {
            command = BEFORE_COMMAND+serverDto.getPort()+AFTER_COMMAND;
        }
        try {
            Wini wini = new Wini(new File(runPath));
            Profile.Section section = wini.get(SERVICE);
            section.put("Environment", command);
            wini.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean compare = true;
        try {
            Runtime.getRuntime().exec(RESTART_COMMAND, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        syncConfig(serverDto);
        Boolean result = ldapConfigService.linuxCommand(SERVER_NAME);
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
        //配置log文件目录
        stringBuilder.append("TLSVerifyClient" + SPACE_DATA + serverDto.getSslAuthStrategy() + FEED);
        return stringBuilder.toString();
    }
}
