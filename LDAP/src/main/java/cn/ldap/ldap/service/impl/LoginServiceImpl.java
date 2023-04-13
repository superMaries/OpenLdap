package cn.ldap.ldap.service.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.entity.Permission;
import cn.ldap.ldap.common.entity.UserAccountModel;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.UserTypeEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.ConfigMapper;
import cn.ldap.ldap.common.mapper.UserAccountMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.SessionUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.UserTokenInfo;
import cn.ldap.ldap.hander.InitConfigData;
import cn.ldap.ldap.service.LoginService;
import cn.ldap.ldap.service.PermissionService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String TOKEN_SECRET_KEY = "ldapKey";
    /**
     * 密码长度限制
     */
    private static final Integer PASSWORD_LENGTH = 16;

    private static final Integer TOKEN_ID = 0;

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
    private static final String RESULT_ERR = "用户名不正确";
    /**
     * 初始化
     */
    private static final Integer IS_INIT = 1;

    /**
     * 已经初始化描述
     */
    private static final String IS_INIT_STR = "已初始化";

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

    private static final Integer IF_ENABLE = 1;

    private static final Integer SIZE = 1;

    static final String AUTHORIZATION = "auth";
    @Resource
    private PermissionService permissionService;

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserServiceImpl userService;


    /**
     * 下载客户端工具实现类
     *
     * @param httpServletResponse
     * @return
     */
    @Override
    public Boolean downClientTool(HttpServletResponse httpServletResponse) {
        String path = clientToolPath;
        log.info("下载地址为:{}", path);
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
    public ResultVo<Map<String, String>> getVersion() {
        Map<String, String> versionResultMap = new HashMap<>();
        versionResultMap.put(CLIENT_VERSION, clientVersion);
        versionResultMap.put(SERVICE_VERSION, serviceVersion);
        return ResultUtil.success(versionResultMap);
    }

    /**
     * 获取用户证书
     *
     * @return
     * @throws IOException
     */
    @Override
    public byte[] downloadManual() {

        String filePath = manualPath; // 本地Word文档的路径
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            StreamUtils.copy(in, out);
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public ResultVo<List<Permission>> queryMenus(HttpServletRequest request) {
        log.info("获取菜单");
        //获取当前登录的用户信息
        LoginResultVo userInfo = SessionUtil.getUserInfo(request);
        if (ObjectUtils.isEmpty(userInfo) || ObjectUtils.isEmpty(userInfo.getUserInfo())) {
            log.info(USER_NOT_LOGIN.getMessage());
           return ResultUtil.fail(USER_NOT_LOGIN);
        }
        Integer roleId = userInfo.getUserInfo().getRoleId();

        //查询一级菜单
        List<Permission> permissions = permissionService.list
                (new LambdaQueryWrapper<Permission>()
                        .isNull(Permission::getParentId)
                        .eq((!Objects.equals(roleId, StaticValue.ADMIN_ID)), Permission::getRoleId, roleId));

        try {
            //获取子集菜单
            List<Integer> parentIds = permissions.stream().map(it -> it.getId()).collect(Collectors.toList());
            List<Permission> parentPermissionList = permissionService
                    .list(new LambdaQueryWrapper<Permission>()
                            .in(Permission::getParentId, parentIds));

            //解析菜单
            permissions.forEach(it -> {
                List<Permission> childrens = parentPermissionList.stream().
                        filter(child -> child.getParentId().equals(it.getId()))
                        .collect(Collectors.toList());
                it.setChildren(childrens);
            });
        } catch (Exception e) {
            log.error("获取菜单错误：{}", e.getMessage());
        }
        return ResultUtil.success(permissions);
    }

    /**
     * 是否初始化
     *
     * @return
     */
    @Override
    public ResultVo<String> whetherInit() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)) {
            return ResultUtil.fail(NO_CONFIG);
        }
//        if (IS_INIT.equals(config.getIsInit())) {
//            return ResultUtil.success(IS_INIT_STR);
//        } else {
//            return ResultUtil.success(IS_NOT_INIT_STR);
//        }
        return ResultUtil.success(IS_NOT_INIT_STR);
    }

    /**
     * 获取服务模式
     *
     * @return
     */
    @Override
    public ResultVo<String> getServerConfig() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)) {
            return ResultUtil.fail(NO_CONFIG);
        }
        if (MAIN_SERVICE_STATUS.equals(config.getServiceType())) {
            return ResultUtil.success(MAIN_SERVICE_STR);
        } else {
            return ResultUtil.success(FOLLOW_SERVICE_STR);
        }
    }

    @Override
    public ResultVo<Map<String, Object>> certLogin(UserDto userDto, HttpServletRequest request) {
        log.info(userDto.toString());
//        Map<String, Object> mapObj = userService.init();
//        boolean isInit = (boolean) mapObj.get(IS_INIT_STRING);
//        if (isInit) {
//            return ResultUtil.success(mapObj);
//        }
        Map<String, Object> mapObj = new HashMap<>();
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNull(userDto, userDto.getCertSn())) {
            log.error("登录错误:{}", ExceptionEnum.USER_LOGIN_ERROR.getMessage());
            throw new SystemException(ExceptionEnum.USER_LOGIN_ERROR);
        }

        LambdaQueryWrapper<UserModel> lambdaQueryWrapper = new LambdaQueryWrapper<UserModel>()
                .eq(UserModel::getCertSn, userDto.getCertSn())
                .eq(UserModel::getIsEnable, IF_ENABLE);
        List<UserModel> users = userMapper.selectList(lambdaQueryWrapper);

        if (SIZE != users.size()) {
            //失敗  返回
            log.error("需要初始化" + ExceptionEnum.USER_FAIL.getMessage());
            throw new SystemException(ExceptionEnum.USER_FAIL);
        }
        //开始记录用户信息
        UserModel userInfo = users.get(0);
        Map<String, Object> map = new HashMap<>();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, tokenValidTime);
        String token = JWT.create().withHeader(map)
                .withClaim("certNumber", userInfo.getSignCert())
                .withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(TOKEN_SECRET_KEY));

        log.info("验签开始");


        String key = "0444270bd267987f13b32846abb09c34c7c865b4d1559946b5734275ffc7cbcc932909eb815430ada80537bcd02f094dd1c79b04d90105923f57183ab9f076d36a";

        if (key.length() == 130) {
            //这里需要去掉开始第一个字节 第一个字节表示标记
            key = key.substring(2);
        }
        String xhex = key.substring(0, 64);
        String yhex = key.substring(64, 128);
        ECPublicKeyParameters ecPublicKeyParameters = BCUtil.toSm2Params(xhex, yhex);
        //创建sm2 对象
        SM2 sm2 = new SM2(null, ecPublicKeyParameters);
        //这里需要手动设置，sm2 对象的默认值与我们期望的不一致 , 使用明文编码
        sm2.usePlainEncoding();
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        boolean verify = sm2.verify(userDto.getSignCert().getBytes(), HexUtil.decodeHex(userDto.getSignData()));
        if (!verify) {
            throw new SystemException(VERIFY_FAIL);
        }
        log.info("验签成功");
        UserTokenInfo tokenInfo = new UserTokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setRoleId(userInfo.getRoleId());
        tokenInfo.setRoleName(UserTypeEnum.USER_ADMIN.getName(userInfo.getRoleId()));
        //证书名称
        tokenInfo.setCertName(userInfo.getCertName());
        //证书序列号
        tokenInfo.setCertNum(userInfo.getCertSn());
        //签名证书
        tokenInfo.setCertData(userInfo.getSignCert());
        tokenInfo.setId(userInfo.getId());
        log.info("获取token" + token);

        tokenInfo.setServiceType(InitConfigData.getServiceType());
        tokenInfo.setIsSync(InitConfigData.getIsSync());

        LoginResultVo loginResultVo = new LoginResultVo(token, tokenInfo);
        mapObj.put("data", loginResultVo);
        HttpSession session = request.getSession();
        session.setAttribute(AUTHORIZATION, loginResultVo);
        return ResultUtil.success(mapObj);
    }/**/

    /**
     * 账号密码登录
     *
     * @param loginDto
     * @return
     */
    @Override
    public ResultVo<Object> login(LoginDto loginDto, HttpServletRequest request) {

        Map<String, Object> resultMap = new HashMap<>();
        if (ObjectUtils.isEmpty(loginDto.getUserName())) {
            return ResultUtil.fail(USER_NAME_FAIL);
        }
        if (ObjectUtils.isEmpty(loginDto.getPassword())) {
            return ResultUtil.fail(USER_PASSWORD_FAIL);
        }
        if (PASSWORD_LENGTH < loginDto.getPassword().length()) {
            return ResultUtil.fail(MORE_PASSWORD_LENGTH);
        }

        //查询数据库判断账号密码是否正确
        List<UserAccountModel> userAccountModels = userAccountMapper.selectList(new LambdaQueryWrapper<UserAccountModel>()
                .eq(UserAccountModel::getAccount, loginDto.getUserName())
                .eq(UserAccountModel::getPassword, loginDto.getPassword())
                .orderByDesc(UserAccountModel::getId));
        if (ObjectUtils.isEmpty(userAccountModels)) {
            log.info(USER_ACCOUNT_ERROR.getMessage());
            return ResultUtil.fail(ExceptionEnum.USER_ACCOUNT_ERROR);
        }
        Map<String, Object> hashMap = new HashMap<>();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, tokenValidTime);
        String token = JWT.create().withHeader(hashMap)
                .withClaim("userName", loginDto.getUserName())
                .withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(TOKEN_SECRET_KEY));
        UserTokenInfo tokenInfo = new UserTokenInfo();
        tokenInfo.setId(TOKEN_ID);
        tokenInfo.setRoleId(TOKEN_ID);
        tokenInfo.setRoleName(loginDto.getUserName());
        tokenInfo.setToken(token);

        tokenInfo.setServiceType(InitConfigData.getServiceType());
        tokenInfo.setIsSync(InitConfigData.getIsSync());

        LoginResultVo loginResultVo = new LoginResultVo(token, tokenInfo);
        resultMap.put("data", loginResultVo);
        HttpSession session = request.getSession();
        session.setAttribute(AUTHORIZATION, loginResultVo);
        return ResultUtil.success(loginResultVo);
    }

    @Override
    public ResultVo<Boolean> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return ResultUtil.success(true);
    }

    /**
     * USEBkey登录是否展示
     *
     * @return true 显示 false 不显示
     */
    @Override
    public ResultVo<Boolean> isShowUsbKey() {
        List<UserModel> userModels = null;
        try {
            userModels = userMapper.selectList(null);
        } catch (Exception e) {
            log.error("错误日志:{}", e.getMessage());
            throw new SystemException(SQL_ERROR);
        }

        if (ObjectUtils.isEmpty(userModels)) {
            return ResultUtil.success(false);
        } else {
            return ResultUtil.success(true);
        }

    }
}