package cn.ldap.ldap.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.ldap.ldap.common.entity.SSLConfig;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.mapper.SSLConfigMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.SSLConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
@Service
@Slf4j
public class SSLConfigServiceImpl extends ServiceImpl<SSLConfigMapper, SSLConfig> implements SSLConfigService {

    private static final String GANG = "---";

    @Override
    public ResultVo<SSLConfig> queryServerConfig() {
        SSLConfig ssl = getOne(null);

        if (ObjectUtil.isEmpty(ssl)) {
            return ResultUtil.fail(ExceptionEnum.COLLECTION_EMPTY);
        } else {
//ca证书
            StringBuilder caStr = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ssl.getCaName()))) {
                String caLine;
                while ((caLine = bufferedReader.readLine()) != null) {
                    if (!caLine.startsWith(GANG)) {
                        caStr.append(caLine);
                    }
                }
                ssl.setCaCertBase64(caStr.toString());
            } catch (IOException e) {
                ssl.setCaCertBase64("");
            }

//服务器证书
            StringBuilder serverStr = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ssl.getServerName()))) {
                String serverLine;
                while ((serverLine = bufferedReader.readLine()) != null) {
                    if (!serverLine.startsWith(GANG)) {
                        serverStr.append(serverLine);
                    }
                }
                ssl.setServerCertBase64(serverStr.toString());
            } catch (IOException e) {
                ssl.setServerCertBase64("");
            }
            //密钥
            StringBuilder keyStr = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ssl.getKeyName()))) {
                String keyLine;
                while ((keyLine = bufferedReader.readLine()) != null) {
                    if (!keyLine.startsWith(GANG)) {
                        keyStr.append(keyLine);
                    }
                }
                ssl.setKeyBase64(keyStr.toString());
            } catch (IOException e) {
                ssl.setKeyBase64("");
            }
            return ResultUtil.success(ssl);
        }

    }
}
