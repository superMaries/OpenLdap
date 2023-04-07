package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String START_FAIL = "开启服务失败";

    private static final String STOP_SUCCESS = "关闭服务成功";

    private static final String STOP_FAIL = "关闭服务失败";

    private static final String START_COMMAND="systemctl start slapd.service";

    private static final String STOP_COMMAND="systemctl stop slapd.service";

    private static final String SYSTEM = "systemctl";

    private static final String IS_ACTIVE = "is-active";

    private static final String SERVER_NAME = "slapd";

    private static final String ACTIVATING = "activating";

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
        if(BeanUtil.isEmpty(openOrClose) || openOrClose!= Boolean.TRUE || openOrClose != Boolean.FALSE){
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }

        if (openOrClose) {
            Runtime.getRuntime().exec(START_COMMAND, null);
            log.info("开启命令执行:{}",START_COMMAND);
            Boolean result = linuxCommand(SERVER_NAME);
            if (result){
                return ResultUtil.success(START_SUCCESS);
            }else {
                return ResultUtil.fail(START_FAIL);
            }
        } else {
            Runtime.getRuntime().exec(STOP_COMMAND, null);
            log.info("关闭命令:{}", "systemctl stop slapd.service");
            Boolean result = linuxCommand(SERVER_NAME);
            if (result){
                return ResultUtil.success(STOP_SUCCESS);
            }else {
                return ResultUtil.fail(STOP_FAIL);
            }
        }
    }

    /**
     * 判断服务状态
     *
     * @return
     */
    @Override
    public Boolean getServerStatus() {
        String serviceName = SERVER_NAME;
        return linuxCommand(serviceName);
    }

    /**
     * 执行命令判断是否启动状态-----仅限于service服务
     * @param serverName
     * @return
     */
    public Boolean linuxCommand(String serverName){
        try {
            Process process = new ProcessBuilder(SYSTEM, IS_ACTIVE, serverName).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String outPut = reader.readLine();
            log.info("服务信息------:{}", outPut);
            if (ACTIVATING.equals(outPut)) {
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
        //todo 文件名不让修改，拆分接口
        if (multipartFile.isEmpty()) {
            throw new SystemException(FILE_IS_EMPTY);
        }
        //修改fileName
        String fileName = multipartFile.getOriginalFilename();
        String filePath = certPath + fileName;
        File file = new File(filePath);
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
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
