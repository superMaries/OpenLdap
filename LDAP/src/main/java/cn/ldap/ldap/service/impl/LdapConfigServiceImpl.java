package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.entity.ParamConfig;
import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.entity.SSLConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.mapper.ParamConfigMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.LdapConfigService;
import cn.ldap.ldap.service.ParamConfigService;
import cn.ldap.ldap.service.PortLinkService;
import cn.ldap.ldap.service.SSLConfigService;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

import static cn.ldap.ldap.common.enums.ExceptionEnum.*;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
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

    @Value("${canSee.show}")
    private Boolean canSeeIndexDelete;

    @Resource
    private ParamConfigService paramConfigService;

    @Resource
    private SSLConfigService sslConfigService;

    @Resource
    private PortLinkService portLinkService;


    /**
     * 空格数据
     */
    private static final String SPACE_DATA = " ";

    /**
     * 换行
     */
    private static final String FEED = "\n";

    private static final String START = "logfile";

    private static final String START_SUCCESS = "开启服务成功";

    private static final String START_FAIL = "开启服务失败";

    private static final String STOP_SUCCESS = "关闭服务成功";

    private static final String STOP_FAIL = "关闭服务失败";

    private static final String START_COMMAND = "systemctl start slapd.service";

    private static final String STOP_COMMAND = "systemctl stop slapd.service";

    private static final String SYSTEM = "systemctl";

    private static final String IS_ACTIVE = "is-active";

    private static final String SERVER_NAME = "slapd";

    private static final String ACTIVATING = "activating";

    private static final String ACTIVE = "active";

    private static final String CASERVER_CERT = "ca.cer";

    private static final String SERVER_CERT = "server.cer";

    private static final String SERVER_KEY = "server.key";

    private static final String LOG_FILE = "logfile";

    private static final String LOG_LEVEL = "loglevel";
    /**
     * 配置日志大小
     */
    private static final String LOGFILE_ROTATE = "logfile-rotate";

    private static final String SLAP_INDEX = "slapindex";

    //添加配置
    @Override
    public ResultVo<T> addConfig(MainConfig mainConfig) throws IOException {
        ParamConfig paramConfig = paramConfigService.getOne(null);
        if (mainConfig.getLogSize() <= 1 || mainConfig.getLogSize() > 99) {
            throw new SysException(PARAM_ERROR, "日志大小范围在1~99");
        }
        if (!ObjectUtil.isEmpty(paramConfig)) {
            if (!BeanUtil.isEmpty(mainConfig.getLogLevel())) {
                paramConfig.setLogLevel(mainConfig.getLogLevel());
            }
            if (!BeanUtil.isEmpty(mainConfig.getLogLevelDirectory())) {
                paramConfig.setLogFile(mainConfig.getLogLevelDirectory());
                int i = mainConfig.getLogLevelDirectory().lastIndexOf("/");
                String filePath = mainConfig.getLogLevelDirectory().substring(0, i);
                String[] split = mainConfig.getLogLevelDirectory().split("/");
                File file = new File(filePath);
                if (!file.exists()) {
                    return ResultUtil.fail(FILE_PATH_NOT_EXIST);
                }
                String fileName = split[split.length - 1];
                if ("" != fileName && !fileName.endsWith(".log")) {
                    return ResultUtil.fail(FILE_LOG);
                }
            }
            if (BeanUtil.isEmpty(mainConfig.getOpenAcl())) {
                return ResultUtil.fail(ACL_FAIL);
            }
            paramConfig.setOpenAcl(mainConfig.getOpenAcl());
            paramConfig.setLogSize(mainConfig.getLogSize());
            paramConfigService.updateById(paramConfig);
        }

        File file = new File(configPath);
        if (!file.exists()) {
            return ResultUtil.fail(FILE_NOT_EXIST);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String fileName = configPath;
        Boolean found = false;
        Boolean acc = false;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String lineStr = bufferedReader.readLine();

            while (lineStr != null) {
                if (mainConfig.getOpenAcl().equals("false") && lineStr.equals("access to * by * none")) {
                    stringBuilder.append("").append(FEED);
                    acc = true;
                } else if (lineStr.startsWith(START)) {
                    stringBuilder//配置log文件目录
                            .append(LOG_FILE).append(SPACE_DATA).append(mainConfig.getLogLevelDirectory()).append(FEED);
                    //配置之日志输出等级
                    // .append(LOG_LEVEL).append(SPACE_DATA).append(mainConfig.getLogLevel()).append(FEED);
                    found = true;
                } else if (lineStr.startsWith(LOG_LEVEL)) {
                    stringBuilder.append(LOG_LEVEL).append(SPACE_DATA).append(mainConfig.getLogLevel()).append(FEED);
                    found = true;
                } else if (lineStr.startsWith(LOGFILE_ROTATE)) {
                    String data = StaticValue.THREE + SPACE_DATA + mainConfig.getLogSize() + SPACE_DATA + 0;
                    stringBuilder.append(LOGFILE_ROTATE).append(SPACE_DATA).append(data).append(FEED);
                    found = true;
                } else {
                    stringBuilder.append(lineStr).append(FEED);
                }
                lineStr = bufferedReader.readLine();
                //  String oldData = lineStr;
                //  stringBuilder.append(oldData).append(FEED);
            }
            bufferedReader.close();

            if (!found) {
                stringBuilder.append(LOG_FILE).append(SPACE_DATA).append(mainConfig.getLogLevelDirectory()).append(FEED)
                        //配置之日志输出等级
                        .append(LOG_LEVEL).append(SPACE_DATA).append(mainConfig.getLogLevel()).append(FEED);
            }

            if (!acc) {
                stringBuilder.append("access to * by * none").append(FEED);
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new SysException(SYSTEM_CONFIG_ERRROR);
        }
        String data = stringBuilder.toString();
        try {
            //采用流的方式进行写入配置
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            //     bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    public ResultVo<String> setServerStatus(Boolean openOrClose) throws IOException {
        if (BeanUtil.isEmpty(openOrClose)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }

        //判断当前进程中是否有slapindex进程在运行
        Boolean isRunning = false;
        try {
            Process exec = Runtime.getRuntime().exec("ps -ef | grep " + SLAP_INDEX);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(SLAP_INDEX)) {
                    isRunning = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new SysException(e.getMessage());
        }


        //如果有正在运行的程序会报错
        if(isRunning){
            return ResultUtil.fail(ExceptionEnum.INDEX_WAITTING);
        }


        if (openOrClose) {

            //开启
            Runtime.getRuntime().exec(START_COMMAND, null);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new SysException(THREAD_SLEEP_ERROR);
            }

            updatePortStatus(String.valueOf(StaticValue.TRUE));
            log.info("开启命令执行:{}", START_COMMAND);
            Boolean result = linuxCommand(SERVER_NAME);
            if (result) {
                return ResultUtil.success(START_SUCCESS);
            } else {
                return ResultUtil.fail(START_FAIL);
            }
        } else {
            //关闭
            Runtime.getRuntime().exec(STOP_COMMAND, null);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new SysException(THREAD_SLEEP_ERROR);
            }

            updatePortStatus(String.valueOf(StaticValue.FALSE));
            Boolean result = linuxCommand(SERVER_NAME);
            if (result) {
                return ResultUtil.fail(STOP_FAIL);
            } else {
                return ResultUtil.success(STOP_SUCCESS);
            }
        }
    }

    public void updatePortStatus(String status) {
        List<PortLink> list = portLinkService.list();
        for (PortLink portLink : list) {
            portLink.setStatus(status);
        }
        portLinkService.updateBatchById(list);
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
     *
     * @param serverName
     * @return
     */
    public Boolean linuxCommand(String serverName) {

        try {
            Process process = new ProcessBuilder(SYSTEM, IS_ACTIVE, serverName).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String outPut = reader.readLine();
            log.info("服务信息------:{}", outPut);
            if (ACTIVATING.equals(outPut) || ACTIVE.equals(outPut)) {
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
    public ResultVo<T> uploadCACert(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new SysException(FILE_IS_EMPTY);
        }
        //修改fileName
        String fileName = CASERVER_CERT;
        String filePath = certPath + fileName;
        File file = new File(filePath);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(FILE_NOT_EXIST);
        }
        List<SSLConfig> list = sslConfigService.list();
        if (CollectionUtils.isEmpty(list) || list.size() > 1) {
            return ResultUtil.fail(ExceptionEnum.DATABASE_ERROR);
        }
        SSLConfig sslConfig = list.get(0);
        sslConfig.setCaName(filePath);
        sslConfigService.updateById(sslConfig);
        return ResultUtil.success();
    }

    @Override
    public ResultVo<T> uploadCert(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new SysException(FILE_IS_EMPTY);
        }
        //修改fileName
        String fileName = SERVER_CERT;
        String filePath = certPath + fileName;
        File file = new File(filePath);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(FILE_NOT_EXIST);

        }
        List<SSLConfig> list = sslConfigService.list();
        if (CollectionUtils.isEmpty(list) || list.size() > 1) {
            return ResultUtil.fail(ExceptionEnum.DATABASE_ERROR);
        }
        SSLConfig sslConfig = list.get(0);
        sslConfig.setServerName(filePath);
        sslConfigService.updateById(sslConfig);
        return ResultUtil.success();
    }

    @Override
    public ResultVo<T> uploadKey(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new SysException(FILE_IS_EMPTY);
        }
        //修改fileName
        String fileName = SERVER_KEY;
        String filePath = certPath + fileName;
        File file = new File(filePath);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {

            log.error(e.getMessage());
            throw new SysException(FILE_NOT_EXIST);
        }
        List<SSLConfig> list = sslConfigService.list();
        if (CollectionUtils.isEmpty(list) || list.size() > 1) {
            return ResultUtil.fail(ExceptionEnum.DATABASE_ERROR);
        }
        SSLConfig sslConfig = list.get(0);
        sslConfig.setKeyName(filePath);
        sslConfigService.updateById(sslConfig);
        return ResultUtil.success();
    }

    @Override
    public ResultVo<Boolean> canSeeDelete() {
        return ResultUtil.success(canSeeIndexDelete);
    }

    /**
     * 拼接参数方法
     *
     * @param mainConfig
     * @return
     */
    public String splicingConfigParam(StringBuilder stringBuilder, MainConfig mainConfig) {

        String command = "";
        if (mainConfig.getOpenAcl().equals("true")) {
            String openAcl = "access to * by * read";
            command = openAcl;
        } else {
            String closeAcl = "access to * by * none";
            command = closeAcl;
        }

        stringBuilder
                //配置是否开启匿名访问
                .append(command).append(FEED)
                //配置log文件目录
                .append(LOG_FILE).append(SPACE_DATA).append(mainConfig.getLogLevelDirectory()).append(FEED)
                //配置之日志输出等级
                .append(LOG_LEVEL).append(SPACE_DATA).append(mainConfig.getLogLevel()).append(FEED);
        return stringBuilder.toString();
    }
}
