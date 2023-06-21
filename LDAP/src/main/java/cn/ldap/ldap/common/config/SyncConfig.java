//package cn.ldap.ldap.common.config;
//
//import cn.ldap.ldap.common.exception.SysException;
//import cn.ldap.ldap.service.impl.SyncServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.io.*;
//
//import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;
//
///**
// * @author suntao
// * @create 2023/6/20
// */
//@Slf4j
//@Configuration
//public class SyncConfig {
//
//
//
//    @Value("${filePath.sync}")
//    private Integer sync;
//
//
//    /**
//     * 配置文件所在路径
//     */
//    @Value("${filePath.configPath}")
//    private String configPath;
//
//    @PostConstruct
//    public static void  getSync(){
//        sync = this.sync;
//    }
//
//    @PostConstruct
//    public void  getConfigPath(){
//        configPath = this.configPath;
//    }
//
//
//    /**
//     * 换行
//     */
//    private static final String FEED = "\n";
//
//
//    private static final String FIRST = "overlay syncprov";
//
//    static {
//
//        settingSync();
//    }
//
//
//    public static void settingSync (){
//        if (sync == 1){
//            File file = new File();
//            if (!file.exists()) {
//                throw new SysException(FILE_NOT_EXIST);
//            }
//            StringBuilder stringBuilder = new StringBuilder();
//            String fileName = configPath;
//            log.info("设置文件");
//            try {
//                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
//                String lineStr = null;
//                while ((lineStr = bufferedReader.readLine()) != null) {
//                    if (lineStr.trim().startsWith(FIRST)) {
//                        break;
//                    }
//                    String oldData = lineStr;
//                    stringBuilder.append(oldData).append(FEED);
//                }
//
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            SyncServiceImpl syncService = new SyncServiceImpl();
//            String data = syncService.splicingConfigParam(stringBuilder, null);
//            try {
//                log.info("写入文件");
//                //采用流的方式进行写入配置
//                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
//                bufferedWriter.write(data);
//                bufferedWriter.flush();
//                bufferedWriter.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}
