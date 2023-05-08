package cn.ldap.ldap.service;

import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PortLinkService extends IService<PortLink> {

    ResultVo<List<PortLink>> getPortLinkList();
}
