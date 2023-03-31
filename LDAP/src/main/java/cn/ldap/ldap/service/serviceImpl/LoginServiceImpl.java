package cn.ldap.ldap.service.serviceImpl;

import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {


    /**
     * 客户端下载工具路径
     */
    @Value("${filePath.clientToolPath}")
    private String clientToolPath;
    /**
     * 用户使用手册路径
     */
    @Value("${filePath.manualPath}")
    private String manualPath;
    /**
     * 客户端版本号
     */
    @Value("${version.clientVersion}")
    private String clientVersion;

    /**
     * 服务版本号
     * @param httpServletResponse
     * @return
     */
    @Value("${version.serviceVersion}")
    private String serviceVersion;
    /**
     * 客户端版本key值
     */
    private static final String CLIENT_VERSION = "clientVersion";
    /**
     * 服务端版本key值
     */
    private static final String SERVICE_VERSION = "serviceVersionKey";

    /**
     * 下载客户端工具实现类
     * @param httpServletResponse
     * @return
     */
    @Override
    public Boolean downClientTool(HttpServletResponse httpServletResponse) {
        String path = clientToolPath;
        log.info("下载地址为：" + path);
        File downFile = new File(path);
        if (downFile.exists()) {
            httpServletResponse.setCharacterEncoding("UTF-8");
            try {
                httpServletResponse.setHeader("Content-Disposition", "attachment;fileName=" +
                        URLEncoder.encode(downFile.getName(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                log.error("/downClientTool/设置返回头信息失败！！！");
            }
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(downFile);
                bis = new BufferedInputStream(fis);
                OutputStream os = httpServletResponse.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e) {
                log.error("下载文件失败！！！" + e.getMessage());
                throw new SystemException("下载文件失败!");
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        } else {
            throw new SystemException("文件不存在!");
        }
    }

    /**
     * 获取版本号
     * @return
     */
    @Override
    public Map<String, String> getVersion() {
        Map<String,String> versionResultMap = new HashMap<>();
        versionResultMap.put(CLIENT_VERSION,clientVersion);
        versionResultMap.put(SERVICE_VERSION,serviceVersion);
        return versionResultMap;
    }

    /**
     * 获取用户证书
     * @return
     * @throws IOException
     */
    @Override
    public byte[] downloadManual() throws IOException {

        String filePath = manualPath; // 本地Word文档的路径
        InputStream in = new FileInputStream(filePath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out);
        out.close();
        in.close();
        log.info(Arrays.toString(out.toByteArray()));
        return out.toByteArray();
    }

}
