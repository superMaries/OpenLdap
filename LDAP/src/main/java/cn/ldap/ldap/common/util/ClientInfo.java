package cn.ldap.ldap.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * @title: ClientInfo
 * @Author Wy
 * @Date: 2023/3/31 16:25
 * @Version 1.0
 */
@Slf4j
public class ClientInfo {
    public static String getIpAdrress(HttpServletRequest request) {
        String  unknow="unknown";
        String localHost="127.0.0.1";
        String macLocal="0:0:0:0:0:0:0:1";
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || unknow.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || unknow.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || unknow.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (localHost.equals(ipAddress) || macLocal.equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    log.error("根据网卡获取本机配置的IP异常=>", e.getMessage());
                }
                if (inet.getHostAddress() != null) {
                    ipAddress = inet.getHostAddress();
                }
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
}
