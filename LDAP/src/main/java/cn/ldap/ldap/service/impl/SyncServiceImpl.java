package cn.ldap.ldap.service.impl;

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

    public String splicingConfigParam(StringBuilder stringBuilder, SyncDto syncDto) {
        //配置log文件目录
        stringBuilder
                .append("syncprov-checkpoint" + SPACE_DATA + syncDto.getTriggerSyncMaxNum() + SPACE_DATA + syncDto.getSyncTimeInterval() + FEED);
        return stringBuilder.toString();
    }
}
