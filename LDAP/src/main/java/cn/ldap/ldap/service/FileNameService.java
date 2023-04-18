package cn.ldap.ldap.service;

import cn.ldap.ldap.common.entity.FileName;
import cn.ldap.ldap.common.vo.ResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @title:
 * @Author superMarie
 * @Version 1.0
 */
public interface FileNameService extends IService<FileName> {

    ResultVo<String> queryFileName();
}
