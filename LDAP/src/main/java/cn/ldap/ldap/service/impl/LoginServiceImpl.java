package cn.ldap.ldap.service.impl;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.PermissionDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.UserTypeEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.ConfigMapper;
import cn.ldap.ldap.common.mapper.PermissionMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.UserTokenInfo;
import cn.ldap.ldap.service.LoginService;
import cn.ldap.ldap.service.PermissionService;
import cn.ldap.ldap.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import static cn.ldap.ldap.common.enums.ExceptionEnum.*;

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
     *
     * @param httpServletResponse
     * @return
     */
    @Value("${version.serviceVersion}")
    private String serviceVersion;

    @Value("${token.validTime}")
    private Integer tokenValidTime;

    static final String TOKEN_SECRET_KEY = "ldapKey";
    /**
     * 密码长度限制
     */
    static final Integer PASSWORD_LENGTH = 16;

    /**
     * 客户端版本key值
     */
    private static final String CLIENT_VERSION = "clientVersion";
    /**
     * 服务端版本key值
     */
    private static final String SERVICE_VERSION = "serviceVersionKey";
    /**
     * 账户名唯一值
     */
    private static final String USER_NAME = "admin";
    /**
     * 用户密码唯一值
     */
    private static final String USER_PASSWORD = "admin";
    /**
     * 用户名不正确返回值
     */
    private static final String RESULT_USER_NAME_ERR = "用户名不正确";
    /**
     * 密码不正确返回值
     */
    private static final String RESULT_PASSWORD_REE = "密码不正确";
    /**
     * 登录成功返回值
     */
    private static final String LOGIN_SUCCESS = "登录成功";
    /**
     * 初始化
     */
    private static final Integer IS_INIT = 1;

    /**
     * 已经初始化描述
     */
    private static final String IS_INIT_STR = "已初始化";

    /**
     * 未初始化
     */
    private static final Integer IS_NOT_INIT = 0;
    /**
     * 未初始化描述
     */
    private static final String IS_NOT_INIT_STR = "未初始化";
    /**
     * 主服务器描述
     */
    private static final String MAIN_SERVICE_STR = "主服务器";

    /**
     * 从服务器描述
     */
    private static final String FOLLOW_SERVICE_STR = "从服务器";

    private static final Integer MAIN_SERVICE_STATUS = 0;

    private static final Integer FOLLOW_SERVICE_STATUS = 1;

    static final String AUTHORIZATION = "Authorization";
    @Resource
    private PermissionService permissionService;

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;


    /**
     * 下载客户端工具实现类
     *
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
     *
     * @return
     */
    @Override
    public Map<String, String> getVersion() {
        Map<String, String> versionResultMap = new HashMap<>();
        versionResultMap.put(CLIENT_VERSION, clientVersion);
        versionResultMap.put(SERVICE_VERSION, serviceVersion);
        return versionResultMap;
    }

    /**
     * 获取用户证书
     *
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

    /**
     * 查看菜单
     *
     * @param
     * @return
     */
    @Override
    public List<Permission> queryMenus() {
        return permissionService.list();
    }

    /**
     * 是否初始化
     *
     * @return
     */
    @Override
    public ResultVo whetherInit() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)) {
            throw new SystemException(NO_CONFIG);
        }
        if(config.getIsInit() ==IS_INIT ){
            return ResultUtil.success(IS_INIT_STR);
        }else {
            return ResultUtil.success(IS_NOT_INIT_STR);
        }

    }

    /**
     * 获取服务模式
     *
     * @return
     */
    @Override
    public ResultVo getServerConfig() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)) {
            throw new SystemException(NO_CONFIG);
        }
        if (MAIN_SERVICE_STATUS == config.getServiceType()){
            return ResultUtil.success(MAIN_SERVICE_STR);
        }else {
            return ResultUtil.success(FOLLOW_SERVICE_STR);

        }
    }

    @Override
    public Map<String, Object> certLogin(UserDto userDto) {
        log.info(userDto.toString());
        Map<String, Object> mapObj = userService.isInit();
        boolean isInit = (boolean) mapObj.get("isInit");
        if (isInit) {
            return mapObj;
        }

        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNull(userDto, userDto.getCertSn())) {
            log.error(ExceptionEnum.USER_LOGIN_ERROR.getMessage());
            throw new SystemException(ExceptionEnum.USER_LOGIN_ERROR);
        }

        List<UserModel> users = userMapper.selectList(new LambdaQueryWrapper<UserModel>()
                .eq(UserModel::getCertSn, userDto.getCertSn())
                .eq(UserModel::getIsEnable, 1));

        if (1 != users.size()) {
            //失敗  返回
            log.error("需要初始化" + ExceptionEnum.USER_FAIL.getMessage());
            throw new SystemException(ExceptionEnum.USER_FAIL);
        }


        //开始记录用户信息
        UserModel userInfo = users.get(0);
        Map<String, Object> map = new HashMap<>();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, tokenValidTime);
        String certNumber = JWT.create().withHeader(map).withClaim("certNumber", userInfo.getSignCert())
                .withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(TOKEN_SECRET_KEY));

        log.info("验签开始");

        SM2 sm2 = SmUtil.sm2();
        sm2.verify(userInfo.getSignCert().getBytes(), userDto.getSignData().getBytes(), userDto.getCertSn().getBytes());
        log.info("验签成功");
        UserTokenInfo tokenInfo = new UserTokenInfo();
        tokenInfo.setToken(certNumber);
        tokenInfo.setRoleId(userInfo.getRoleId());
        tokenInfo.setRoleName(UserTypeEnum.USER_ADMIN.getName(userInfo.getRoleId()));
        //证书名称
        tokenInfo.setCertName(userInfo.getCertName());
        //证书序列号
        tokenInfo.setCertNum(userInfo.getCertSn());
        //签名证书
        tokenInfo.setCertData(userInfo.getSignCert());
        tokenInfo.setId(userInfo.getId());
        String token = UUID.randomUUID().toString();
        log.info("获取token" + token);
        LoginResultVo loginResultVo = new LoginResultVo(token, tokenInfo);
        mapObj.put("data", loginResultVo);
        return mapObj;
    }

    /**
     * 账号密码登录
     *
     * @param loginDto
     * @return
     */
    @Override
    public ResultVo login(LoginDto loginDto) {

        if (ObjectUtils.isEmpty(loginDto.getUserName())) {
            throw new SystemException(USER_NAME_FAIL);
        }
        if (ObjectUtils.isEmpty(loginDto.getPassword())) {
            throw new SystemException(USER_PASSWORD_FAIL);
        }
        if ( PASSWORD_LENGTH < loginDto.getPassword().length()){
            throw new SystemException(MORE_PASSWORD_LENGTH);
        }
        if (!USER_NAME.equals(loginDto.getUserName())) return ResultUtil.fail(RESULT_USER_NAME_ERR);
        if (!USER_PASSWORD.equals(loginDto.getPassword())) return ResultUtil.fail(RESULT_PASSWORD_REE);
        return ResultUtil.success(LOGIN_SUCCESS);
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return true;
    }
}