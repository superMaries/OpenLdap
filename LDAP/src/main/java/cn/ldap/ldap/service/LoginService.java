package cn.ldap.ldap.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface LoginService {
    /**
     * 下载客户端工具
     * @param httpServletResponse
     * @return
     */
    Boolean downClientTool(HttpServletResponse httpServletResponse);

    /**
     * 获取版本号
     * @return
     */
    Map<String,String> getVersion();

    /**
     * 下载用户手册接口
     * @param
     * @return
     */
    byte[] downloadManual() throws IOException;
}
