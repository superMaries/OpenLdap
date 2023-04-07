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
import java.util.Objects;
import java.io.*;

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

    @Value("${filePath.runPath}")
    private String runPath;
    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    /**
     * 换行
     */
    private static final String FEED = "\n";

    private static final String START = "logfile";

    private static final String SERVICE = "Service";

    private static final String START_SUCCESS = "开启服务成功";

    private static final String STOP_SUCCESS = "关闭服务成功";

    //添加配置
    @Override
    public ResultVo<T> addConfig(MainConfig mainConfig) throws IOException {
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
            throw new RuntimeException(e);
        }
        String data = splicingConfigParam(stringBuilder, mainConfig);
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Wini wini = new Wini(new File(runPath));
        Profile.Section section = wini.get(SERVICE);
        section.put("Environment", "SLAPD_URLS=ldaps:0.0.0/// ldapi:///\" \"SLAPD_OPTIONS=");
        wini.store();
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
    public ResultVo<String> setServerStatus(Boolean openOrClose) throws IOException {
        if (openOrClose) {
            Runtime.getRuntime().exec("systemctl start slapd.service", null, new File(System.getProperty("user.dir")));
            log.info("开启命令:{}", "systemctl start slapd.service");
            return ResultUtil.success(START_SUCCESS);
        } else {
            Runtime.getRuntime().exec("systemctl stop slapd.service", null, new File(System.getProperty("user.dir")));
            log.info("关闭命令:{}", "systemctl stop slapd.service");
            return ResultUtil.success(STOP_SUCCESS);
        }

    }

    /**
     * 判断服务状态
     *
     * @return
     */
    @Override
    public Boolean getServerStatus() {
        String serviceName = "slapd";
        try {
            Process process = new ProcessBuilder("systemctl", "is-active", serviceName).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String outPut = reader.readLine();
            log.info("服务信息------:{}", outPut);
            if (outPut.equals("activating")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResultVo<T> uploadFile(MultipartFile multipartFile) {
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
    public String splicingConfigParam(StringBuilder stringBuilder, MainConfig mainConfig) {
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
