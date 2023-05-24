package cn.ldap.ldap.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作类型
 */
public enum OperateTypeEnum {

    //首页
    QUERY_LDAP_NUM(301, "查询LDAP数量"),
    QUERY_NET_SPEED(302, "获取网络吞吐量"),
    QUERY_OS_INFO(303, "获取信息信息"),

    //用户登录、注册、登出
    USER_LOGIN(401, "登录"),
    //    ADMIN_LOGIN(401, "ADMIN登录"),
    USER_REGIS(402, "注册"),
    USER_LOGOUT(403, "登出"),
    USER_INIT(404, "用户初始化"),
    DOWN_CLIENT(405, "下载客户端"),
    LOOK_MANUAl(406, "查看用户手册"),
    QUERY_VERSION(406, "获取版本号"),
    QUERY_MENUS(408, "查看所有菜单接口"),
    USER_IS_INIT(409, "是否初始化"),
    SERVICE_MODEL(410, "获取服务模式"),
    IMPORT_ADMIN_KEY(411, "导出管理员key接口"),

    IMPORT_CONFIG(412, "导入服务配置接口"),


    //操作日志
    OPERATE_QUERY(501, "查询日志"),


    //    目录树
    LOOK_DATA(601, "查询数据"),
    DEL_LDAP(602, "删除节点"),
    EDIT_LDAP_ATTRITE(603, "编辑属性"),
    MIDIFY_LDAP_NAME(604, "修改节点名称"),
    EXPORT_ALL(605, "查询导出全部"),
    EXPORT_LDIF(607, "导入LDIF文件"),
    IMPORT_LDAP(606, "导入LDIF文件"),
    ADD_LDAF(608, "新增LDAP节点"),


    //参数配置
    LOOK_PARAM(701, "查询参数配置"),

    UPDATE_PARAM(702, "更新参数配置"),

    UPLOAD_FILE(703, "上传文件"),

    OPEN_SERVICE(704, "开启或关闭服务"),

    ADD_DATA(705, "添加数据"),

    DEL_USBKEY(801, "删除USBKEY用户"),

    ADD_USBKEY(802, "添加USBKEY用户"),

    UPDATE_USBKEY(802, "修改密码"),


    //索引管理
    INDEX_UPDATE_OR_INTER(901, "更新或者插入"),
    INDEX_DELETE(902, "删除索引"),


    //服务管理
    SERVICE_QUERY(1001, "服务管理"),

    ACCOUNT_ADD(1101,"账户管理添加"),
    ACCOUNT_DELETE(1102,"账户管理删除"),
    ACCOUNT_UPDATE_PASSWORD(1103,"账户管理重置密码"),
    ACCOUNT_UPDATE_AUTH(1104,"账户管理修改权限"),
    ;

    public static Map<String, String> getMap() {
        java.util.Map<java.lang.String, java.lang.String> maps = new HashMap<>();
        for (OperateTypeEnum operateMenuEnum : values()) {
            maps.put(operateMenuEnum.name, operateMenuEnum.name);
        }
        return maps;
    }

    private Integer code;
    private String name;


    OperateTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
