package cn.ldap.ldap.service.serviceImpl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.json.JSONUtil;
import cn.ldap.ldap.common.dto.LoginDto;
import cn.ldap.ldap.common.dto.PermissionDto;
import cn.ldap.ldap.common.dto.UserDto;
import cn.ldap.ldap.common.entity.ConfigModel;
import cn.ldap.ldap.common.entity.Information;
import cn.ldap.ldap.common.entity.UserModel;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.enums.UserTypeEnum;
import cn.ldap.ldap.common.exception.SystemException;
import cn.ldap.ldap.common.mapper.ConfigMapper;
import cn.ldap.ldap.common.mapper.PermissionMapper;
import cn.ldap.ldap.common.mapper.UserMapper;
import cn.ldap.ldap.common.redis.RedisUtils;
import cn.ldap.ldap.common.vo.LoginResultVo;
import cn.ldap.ldap.common.vo.UserRedisInfo;
import cn.ldap.ldap.service.LoginService;
import cn.ldap.ldap.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
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

    /**
     * 登录超时时间
     */
    @Value("${login.timeout}")
    private Integer timeout;

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
    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtils redisUtils;

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
     * @param roleId
     * @return
     */
    @Override
    public List<PermissionDto> queryMenus(Integer roleId) {
        List<PermissionDto> permissions = permissionMapper.queryPermissionList(roleId);
        List<PermissionDto> permissionList = new ArrayList<>();
        for (PermissionDto dto : permissions) {
            if (ObjectUtil.isNull(dto.getParentId())) {
                permissionList.add(dto);
            } else {
                for (PermissionDto it : permissionList) {
                    if (dto.getParentId().equals(it.getId())) {
                        List<PermissionDto> perList = ObjectUtil.isNull(it.getPermissionDtoList()) ? new ArrayList<>() : it.getPermissionDtoList();
                        perList.add(perList.size(), dto);
                        it.setPermissionDtoList(perList);
                        break;
                    }
                }
            }
        }
        return permissionList;
    }

    /**
     * 是否初始化
     * @return
     */
    @Override
    public Integer whetherInit() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)){throw new SystemException(NO_CONFIG);}
        return config.getIsInit();
    }

    /**
     * 获取服务模式
     * @return
     */
    @Override
    public Integer getServerConfig() {
        ConfigModel config = configMapper.getConfig();
        if (ObjectUtils.isEmpty(config)){throw new SystemException(NO_CONFIG);}
        return config.getServiceType();
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
        log.info("验签开始");

        SM2 sm2 = SmUtil.sm2();
        sm2.verify(userInfo.getSignCert().getBytes(), userDto.getSignData().getBytes(), userDto.getCertSn().getBytes());
        log.info("验签成功");
        UserRedisInfo redisInfo = new UserRedisInfo();

        redisInfo.setRoleId(userInfo.getRoleId());
        redisInfo.setRoleName(UserTypeEnum.USER_ADMIN.getName(userInfo.getRoleId()));
        //证书名称
        redisInfo.setCertName(userInfo.getCertName());
        //证书序列号
        redisInfo.setCertNum(userInfo.getCertSn());
        //签名证书
        redisInfo.setCertData(userInfo.getSignCert());
        redisInfo.setId(userInfo.getId());
        log.info("redis 信息" + redisInfo);
        String token = UUID.randomUUID().toString();
        log.info("获取token" + token);
        //开始写入redis
        redisUtils.set(Information.AUTHORIZATION + token, JSONUtil.toJsonStr(redisInfo), timeout);
        LoginResultVo loginResultVo = new LoginResultVo(token, redisInfo);
        mapObj.put("data", loginResultVo);
        return mapObj;
    }

    /**
     * 账号密码登录
     * @param loginDto
     * @return
     */
    @Override
    public String login(LoginDto loginDto) {
        if (ObjectUtils.isEmpty(loginDto.getUserName())){throw new SystemException(USER_NAME_FAIL);}
        if (ObjectUtils.isEmpty(loginDto.getPassword())){throw new SystemException(USER_PASSWORD_FAIL);}
        if (!USER_NAME.equals(loginDto.getUserName())) return RESULT_USER_NAME_ERR;
        if (!USER_PASSWORD.equals(loginDto.getPassword())) return RESULT_PASSWORD_REE;
        return LOGIN_SUCCESS;
    }
}