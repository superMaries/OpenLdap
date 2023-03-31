package cn.ldap.ldap.service;

import cn.ldap.ldap.common.dto.UserDto;
import com.sun.xml.internal.ws.server.ServerRtException;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @title: UserService
 * @Author Wy
 * @Date: 2023/3/31 8:58
 * @Version 1.0
 */
public interface UserService {
    Map<String,Object> isInit();
    boolean importConfig(UserDto userDto);
    boolean importAdminKey(UserDto userDto);
}
