package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import cn.ldap.ldap.util.LinuxCmdEnginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_IS_EMPTY;
import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

@Service
@Slf4j
public class LdapConfigServiceImpl implements LdapConfigService {

    /**
     * 配置文件所在路径
     */
    @Value("${filePath.configPath}")
    private String configPath;
    @Value("${filePath.certPath}")
    private String certPath;
    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    /**
     * 换行
     */
    private static final String FEED = "\n";

    //添加配置
    @Override
    public ResultVo addConfig(MainConfig mainConfig) {
        File file = new File(configPath);
        if (!file.exists()) {
            throw new SystemException(FILE_NOT_EXIST);
        }
        String fileName = configPath;
        String data = splicingConfigParam(mainConfig);
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtil.success();
    }

    /**
     * 设置服务状态
     *
     * @param openOrClose
     * @return
     * @throws IOException
     */
    @Override
    public ResultVo setServerStatus(Boolean openOrClose) throws IOException {
        if (openOrClose) {
            Runtime.getRuntime().exec("./slapd", null, new File(System.getProperty("user.dir")));
        } else {
            Runtime.getRuntime().exec("./slapd stop", null, new File(System.getProperty("user.dir")));
        }
        return ResultUtil.success();
    }

    /**
     * 判断服务状态
     *
     * @return
     */
    @Override
    public Boolean getServerStatus() {
        String runStr = "ps -ef|grep slapd";
        ResultVo resultVo = null;
        try {
            resultVo = LinuxCmdEnginUtil.listInfo(runStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer successCode = 1000;
        if (resultVo.getCode() != successCode) {
            return false;
        }
        List list = (List) resultVo.getData();
        if (list.size() > 2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResultVo uploadFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new SystemException(FILE_IS_EMPTY);
        }
        String fileName = multipartFile.getOriginalFilename();
        String filePath = certPath + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResultUtil.success();
    }

    /**
     * 拼接参数方法
     *
     * @param mainConfig
     * @return
     */
    public String splicingConfigParam(MainConfig mainConfig) {
        StringBuilder stringBuilder = new StringBuilder();
        //配置log文件目录

        stringBuilder.append("logfile" + SPACE_DATA + mainConfig.getLogLevelDirectory() + FEED)
                //配置之日志输出等级
                .append("loglevel" + SPACE_DATA + mainConfig.getLogLevel() + FEED)
                //配置主从模式
                .append("overlay" + SPACE_DATA + "syncprov" + FEED)
                //配置推送触发条件
                .append("syncprov-checkpoint" + SPACE_DATA + mainConfig.getTriggerSyncMaxNum() + SPACE_DATA + mainConfig.getSyncTimeInterval() + FEED)
                //配置会话日志最大条数
                .append("syncprov-sessionlog" + SPACE_DATA + mainConfig.getTalkMaxNumber() + FEED)
                .append("TLSVerifyClient" + SPACE_DATA + mainConfig.getAnonymousAccess() + FEED);
        return stringBuilder.toString();
    }
}
