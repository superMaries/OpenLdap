package cn.ldap.ldap.common.enums;

/**
 * 异常枚举
 *
 * @title: UserServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public enum ExceptionEnum {
    //操作成功
    SUCCESS(200, "操作成功"),
    //用户未登录
    USER_NOT_LOGIN(401, "用户未登录"),
    //参数异常
    PARAM_ERROR(201, "参数异常"),
    PASSWD(204, "密码不可为null"),
    ACCOUNT(205, "账号不可为null"),
    //数据存储错误
    SQL_SAVA_ERROR(202, "数据存储错误"),

    //已被初始化
    USER_INIT(203, "已被初始化"),


    //系统异常
    SYSTEM_ERROR(500, "系统异常"),


    //登录失败
    USER_LOGIN_ERROR(581, "登录失败"),
    //用户不存在或已被禁用
    USER_FAIL(583, "用户不存在或已被禁用"),
    //用户不存在
    USER_NAME_FAIL(584, "用户不存在"),
    //密码为空
    USER_PASSWORD_FAIL(585, "密码为空"),
    //密码超过长度限制
    MORE_PASSWORD_LENGTH(586, "密码超过长度限制"),
    USER_ACCOUNT_ERROR(587, "账号或密码错误"),
    REQUEST_WAY_ERROR(588, "请求方式有误,请检查 GET/POST"),
    OLD_PASSWORD_ERROR(589, "旧密码与新密码不符"),
    SYSTEM_CONFIG_ERRROR(590, "系统配置错误,请输入或修改配置文件"),
    //未获取到配置文件信息
    DATABASE_ERROR(799,"SSL配置文件不正确"),
    NO_CONFIG(800, "未获取到配置文件信息"),
    PARAM_EMPTY(801, "参数为空"),

    PARAM_ANOMALY(802, "参数异常"),
    SQL_ERROR(803, "SQL异常"),
    POINTT_NOT_EXIT(804, "节点不存在"),

    QUERY_POINT_ERROR(805, "查询节点失败"),
    DATA_EXIT(806, "数据已存在"),
    READ_FILE_ERROR(808, "IO流异常"),
    NOT_AUDIT(807, "该账号是管理员,不允许审计日志"),
    LINK_ERROR(808, "连接异常"),
    SCHEMA_ERROR(809, "SCHEMA信息异常"),

    FILE_IO_ERROR(810, "文件流处理失败"),
    STR_ERROR(811, "字符格式错误"),
    NODE_NOT_EXIT(812, "节点不存在，重新配置"),
    CER_ERROR(813,"证书或密钥为空，请上传"),

    SIGN_DATA_ERROR(880, "数据不正常，验签错误"),
    //失败

    ACL_FAIL(5004,"请选择是否开启匿名访问"),
    FILE_PATH_NOT_EXIST(5006,"文件路径不存在"),

    FILE_LOG(5005,"请输入以.log结尾的文件"),
    LINUX_ERROR(5007, "失败"),

    //上传失败
    UPLOAD_ERR(5008, "上传失败"),
    //文件不存在
    FILE_NOT_EXIST(5009, "文件不存在"),
    //文件为空
    FILE_IS_EMPTY(5010, "文件为空"),
    //数据为空
    COLLECTION_EMPTY(5011, "数据为空"),
    //验签失败
    VERIFY_FAIL(5012, "验签失败"),

    VALIDATE_ERROR(5013, "验证书链错误"),
    HEADER_ERROR(5014, "未有签名验签的值"),
    NOT_DIRECTORY(5015,"该文件不是个文件夹"),

    //LDAP接口中的错误 全部定义8开头
    LDAP_RDN_NOT_EXIT(8001, "删除RDN错误"),

    LDAP_QUERY_RDN_NOT_EXIT(8002, "解析RDN错误"),

    LDAP_DEL_RDN_NOT_EXIT(8002, "删除RDN错误"),


    LDAP_DATA_ERROR(8004,"缺少从服务连接数据"),

    LDAP_PORT_ERROR(8006,"端口配置相同"),

    LDAP_URL_ERROR(8007,"地址配置错误，URL地址需要以ldap://开始"),
    LDAP_CONNECT_ERROR(8005, "连接LDAP服务异常"),
    LDAP_ERROR(8008, "LDAP服务异常"),
    LDAP_QUERY_ERROR(8008, "LDAP查询错误"),
    LDAP_ERROR_FILTER(8009,"过滤条件为空"),
    CER_NULL_ERROR(8010,"证书为空"),
    KEY_NULL_ERROR(8011,"密钥为空")
            ;

    /**
     * code
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * @param code    code
     * @param message 提示信息
     */
    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取对应的code
     *
     * @return code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取对应的提示信息
     *
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }
}
