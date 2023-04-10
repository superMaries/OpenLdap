package cn.ldap.ldap.service;

import cn.ldap.ldap.common.entity.MainConfig;
import cn.ldap.ldap.common.vo.ResultVo;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface LdapConfigService {

    /**
     * 主服务添加配置
     *
     * @param mainConfig
     * @return
     */
    ResultVo<T>  addConfig(MainConfig mainConfig) throws IOException;

    /**
     * 开启或者关闭服务
     * @param openOrClose
     * @return
     */
    ResultVo<String> setServerStatus(Boolean openOrClose) throws IOException;

    /**
     * 判断服务启动状态
     * @return
     */
    Boolean getServerStatus();

    /**
     * 上传CA证书文件
     * @param multipartFile
     */
    ResultVo<T> uploadCACert(MultipartFile multipartFile);

    /**
     * 上传证书
     * @param multipartFile
     */
    ResultVo<T> uploadCert(MultipartFile multipartFile);

    /**
     * 上传密钥
     * @param multipartFile
     */
    ResultVo<T> uploadKey(MultipartFile multipartFile);
}
