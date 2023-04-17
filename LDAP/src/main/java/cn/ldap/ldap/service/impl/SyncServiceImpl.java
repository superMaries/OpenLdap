package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.FromSyncDto;
import cn.ldap.ldap.common.dto.SyncDto;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;

@Service
@Slf4j
public class SyncServiceImpl implements SyncService {

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

    private static final String START_FROM = "syncrepl";

    private static final String RID="rid=";

    private static final String SYNCREPL = "syncrepl";

    private static final String PROVIDER = "provider=";

    private static final String TYPE = "type=";

    private static final String REFRESH_AND_PERSIST= "refreshAndPersist";

    private static final String INTERVAL = "interval=";

    private static final String SEARCH_BASE = "searchbase=";

    private static final String FILTER = "filter=";

    private static final String OBJ = "(objectClass=*)";

    private static final String SCOPE="scope=";

    private static final String SUB = "sub";

    private static final String SCHEMA_CHECK_ING = "schemachecking=";

    private static final String OFF = "off";

    private static final String BIND_METHOD = "bindmethod=";

    private static final String SIMPLE = "simple";

    private static final String BIND_DN = "binddn=";

    private static final String CREDENTIALS = "credentials=";

    private static final String RETRY ="retry=\"60 +\"";

    /**
     * 主服务同步配置
     * @param syncDto
     * @return
     */
    @Override
    public ResultVo<Object> syncConfig(SyncDto syncDto) {
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
     * @param fromSyncDto
     * @return
     */
    @Override
    public ResultVo<Object> fromSyncConfig(FromSyncDto fromSyncDto) {
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
        String data = splicingConfigFrom(stringBuilder, fromSyncDto);
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
     * 主服务配置数据拼接
     * @param stringBuilder
     * @param syncDto
     * @return
     */
    public String splicingConfigParam(StringBuilder stringBuilder, SyncDto syncDto) {
        //主服务配置文件
        stringBuilder.append(START).append(SPACE_DATA).append(syncDto.getTriggerSyncMaxNum()).append(SPACE_DATA).append(syncDto.getSyncTimeInterval()).append(FEED);
        return stringBuilder.toString();
    }

    /**
     * 从服务配置数据拼接
     * @param stringBuilder
     * @param fromSyncDto
     * @return
     */
    public String splicingConfigFrom(StringBuilder stringBuilder, FromSyncDto fromSyncDto) {
        //从服务配置文件
        stringBuilder.append(SYNCREPL).append(SPACE_DATA).append(RID).append(fromSyncDto.getRid()).append(FEED)
                .append(SPACE_DATA).append(PROVIDER).append(fromSyncDto.getMainServerUrl()).append(FEED)
                .append(SPACE_DATA).append(TYPE).append(REFRESH_AND_PERSIST).append(FEED)
                .append(SPACE_DATA).append(INTERVAL).append(fromSyncDto.getSyncTime())
                .append(SPACE_DATA).append(SEARCH_BASE).append(fromSyncDto.getSyncPoint())
                .append(SPACE_DATA).append(FILTER).append(OBJ)
                .append(SPACE_DATA).append(SCOPE).append(SUB)
                .append(SPACE_DATA).append(SCHEMA_CHECK_ING).append(OFF)
                .append(SPACE_DATA).append(BIND_METHOD).append(SIMPLE)
                .append(SPACE_DATA).append(BIND_DN).append(fromSyncDto.getMainServerAccount())
                .append(SPACE_DATA).append(CREDENTIALS).append(fromSyncDto.getMainServerPassword())
                .append(SPACE_DATA).append(RETRY);
        return stringBuilder.toString();
    }
}
