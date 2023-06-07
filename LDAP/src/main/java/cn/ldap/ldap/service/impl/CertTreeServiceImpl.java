package cn.ldap.ldap.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.ldap.ldap.common.dto.*;
import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.exception.SysException;
import cn.ldap.ldap.common.util.LdapUtil;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.util.StaticValue;
import cn.ldap.ldap.common.vo.CertTreeVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.common.vo.TreeVo;
import cn.ldap.ldap.service.CertTreeService;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static cn.ldap.ldap.common.enums.ExceptionEnum.FILE_NOT_EXIST;
import static cn.ldap.ldap.common.enums.ExceptionEnum.NOT_DIRECTORY;

/**
 * @title: CertTreeServiceImpl
 * @Author Wy
 * @Date: 2023/4/11 9:35
 * @Version 1.0
 */
@Service
@Slf4j
public class CertTreeServiceImpl implements CertTreeService {
    @Resource
    private LdapTemplate ldapTemplate;
    @Value("${ldap.searchBase}")
    private String ldapSearchBase;
    @Value("${ldap.searchFilter}")
    private String ldapSearchFilter;

    /**
     * 换行
     */
    private static final String FEED = "\n";

    private static final String LAST = ".ldif";


    @Value("${command.binFile}")
    private String binFile;

    @Value("${ldap.userDn}")
    private String account;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.searchBase}")
    private String searchBase;

    private static final String FRONT_COMMAND = "./ldapsearch -D ";

    private static final String ONE = " -s one";

    private static final String ALL_FILTER = "(objectClass=*)";

    private static final String BEHIND_COMMAND = " |grep \"#\" |wc -l ";

    private String SPACE = " ";

    private static final String CD = "cd ";

    /**
     * 查询目录树接口
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @Override
    public ResultVo<List<CertTreeVo>> queryCertTree(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            treeVo = new CertTreeDto();
            treeVo.setBaseDN(ldapSearchBase);
            treeVo.setBaseDN(ldapSearchFilter);
        }
        if (ObjectUtils.isEmpty(treeVo.getBaseDN())) {
            treeVo.setBaseDN(ldapSearchBase);
            treeVo.setFilter(ldapSearchFilter);
        }
        if (ObjectUtils.isEmpty(treeVo) || ObjectUtils.isEmpty(treeVo.getBaseDN())) {
            log.info("参数异常；{}", ExceptionEnum.PARAM_ERROR);
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        LdapTemplate newLdapTemplate = fromPool();
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(newLdapTemplate, treeVo.getFilter(), treeVo.getBaseDN(),
                treeVo.getScope(), treeVo.getPageSize(), treeVo.getPage(), null);
        return ResultUtil.success(listResultVo);
    }

    /**
     * 查询节点属性详情
     *
     * @param treeDto 参数
     * @return 返回查询节点属性详情
     */
    @Override
    public ResultVo<List<TreeVo>> queryAttributeInfo(CertTreeDto treeDto) {
        if (ObjectUtils.isEmpty(treeDto)
                || ObjectUtils.isEmpty(treeDto.getBaseDN())) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        LdapTemplate newLdapTemplate = fromPool();
        List<TreeVo> treeVos = LdapUtil.queryAttributeInfo(newLdapTemplate, treeDto.getBaseDN(), treeDto.isReturnAttr(), treeDto.getAttribute());
        return ResultUtil.success(treeVos);
    }

    /**
     * 根据条件查询目录树
     *
     * @param treeVo 参数
     * @return 返回树型结构
     */
    @Override
    public ResultVo<Map<String, Object>> queryTree(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo.getFilter())) {
            return ResultUtil.fail(ExceptionEnum.LDAP_ERROR_FILTER);
        }
        if (ObjectUtils.isEmpty(treeVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        Map<String, Object> map = new HashMap<>();
        LdapTemplate newLdapTemplate = fromPool();

        List<CertTreeVo> certTreeVos = new ArrayList<>();
        long total = 0L;

        try {

            StringBuilder stringBuilderFather = new StringBuilder();
            stringBuilderFather.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(SPACE).append("-w").append(SPACE).append("\"").append(password)
                    .append("\"").append(SPACE).append("-b").append(SPACE).append("\"").append(treeVo.getBaseDN())
                    .append("\"").append(SPACE).append("\"").append(ALL_FILTER).append("\"").append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}", stringBuilderFather);
            ProcessBuilder builderFather = new ProcessBuilder();
            builderFather.command("sh", "-c", stringBuilderFather.toString());
            Process exec = builderFather.start();
            InputStream inputStreamFather = exec.getInputStream();
            BufferedReader bufferedReaderFather = new BufferedReader(new InputStreamReader(inputStreamFather));

            String line;
            while ((line = bufferedReaderFather.readLine()) != null) {
                Long aLong = Long.valueOf(line.trim());
                log.info("Linux查询数量为:{}", aLong);
                if (aLong < 10) {
                    total = 0L;
                } else {
                    total = aLong - 10;
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_ERROR);
        }


        map.put("total", total);
        map.put("page", total / treeVo.getPageSize() + (total % treeVo.getPageSize() != 0 ? 1 : 0));


        LdapUtil.queryCertTree(newLdapTemplate, treeVo.getFilter(), treeVo.getBaseDN(),
                treeVo.getScope(), treeVo.getPageSize(), treeVo.getPage(), map);


        return ResultUtil.success(map);
    }

    /**
     * 只需要传递 rdn  scope 的值 （ one`：搜索指定的DN及其一级子节点。`sub`：搜索指定的DN及其所有子孙节点。）
     *
     * @param treeVo 参数
     * @return 返回一个Map  其中表示rdn 和num
     */
    @Override
    public ResultVo<Map<String, Object>> queryTreeRdnOrNum(CertTreeDto treeVo) {
        if (ObjectUtils.isEmpty(treeVo)) {
            return ResultUtil.fail(ExceptionEnum.PARAM_ERROR);
        }
        Map<String, Object> map = new HashMap<>();


        Long resultFather = 0L;

        Long resultSon = 0L;
        try {
            log.info("切换到可执行命令文件夹:{}", binFile);
            File file = new File(binFile);
            if (!file.exists()) {
                throw new SysException(FILE_NOT_EXIST);
            }
            if (!file.isDirectory()) {
                throw new SysException(NOT_DIRECTORY);
            }

            //拼接linux命令
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(SPACE).append("-w").append(SPACE).append("\"").append(password)
                    .append("\"").append(SPACE).append("-b").append(SPACE).append("\"").append(treeVo.getBaseDN())
                    .append("\"").append(SPACE).append("\"").append(ALL_FILTER).append("\"").append(ONE).append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}", stringBuilder);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", stringBuilder.toString());
            Process exec = builder.start();
            InputStream inputStream = exec.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Long aLong = Long.valueOf(line.trim());
                log.info("Linux查询数量为:{}", aLong);
                if (aLong < 10) {
                    resultSon = 0L;
                } else {
                    resultSon = aLong - 10;
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_ERROR);
        }

        try {

            StringBuilder stringBuilderFather = new StringBuilder();
            stringBuilderFather.append(CD).append(binFile).append(";").append(FRONT_COMMAND).append("\"").append(account)
                    .append("\"").append(SPACE).append("-w").append(SPACE).append("\"").append(password)
                    .append("\"").append(SPACE).append("-b").append(SPACE).append("\"").append(treeVo.getBaseDN())
                    .append("\"").append(SPACE).append("\"").append(ALL_FILTER).append("\"").append(BEHIND_COMMAND);

            log.info("linux运行命令为:{}", stringBuilderFather);
            ProcessBuilder builderFather = new ProcessBuilder();
            builderFather.command("sh", "-c", stringBuilderFather.toString());
            Process exec = builderFather.start();
            InputStream inputStreamFather = exec.getInputStream();
            BufferedReader bufferedReaderFather = new BufferedReader(new InputStreamReader(inputStreamFather));

            String line;
            while ((line = bufferedReaderFather.readLine()) != null) {
                Long aLong = Long.valueOf(line.trim());
                log.info("Linux查询数量为:{}", aLong);
                if (aLong < 10) {
                    resultFather = 0L;
                } else {
                    resultFather = aLong - 10;
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SysException(ExceptionEnum.LDAP_QUERY_ERROR);
        }
        map.put(StaticValue.RDN, treeVo.getBaseDN());
        map.put(StaticValue.RDN_NUM_KEY, resultSon);
        map.put(StaticValue.RDN_CHILD_NUM_KEY, resultFather);
        //--------------------------------------------------------------------
        // LdapTemplate newLdapTemplate = fromPool();
        //map = LdapUtil.queryTreeRdnOrNumEx(map, newLdapTemplate, treeVo.getScope(), treeVo.getBaseDN(), treeVo.getFilter());
        return ResultUtil.success(map);
    }

    /**
     * 删除Ldap
     * 删除节点必须要先删除子节点
     *
     * @param ldapDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> delLdapTreByRdn(LdapDto ldapDto) {
        LdapTemplate newLdapTemplate = fromPool();
        boolean result = LdapUtil.delLdapTreByRdn(newLdapTemplate, ldapDto, ldapSearchFilter);
        return ResultUtil.success(result);
    }

    /**
     * 编辑属性
     *
     * @param ldapBindTreeDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> updateLdapBindTree(LdapBindTreeDto ldapBindTreeDto) {
        LdapTemplate newLdapTemplate = fromPool();
        boolean result = LdapUtil.updateLdapBindTree(newLdapTemplate, ldapBindTreeDto, ldapSearchFilter);
        return ResultUtil.success(true);
    }

    /**
     * 修改节点名称
     *
     * @param bindTree 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> reBIndLdapTree(ReBindTreDto bindTree) {
        LdapTemplate newLdapTemplate = fromPool();
        boolean b = LdapUtil.reBIndLdapTree(newLdapTemplate, bindTree, ldapSearchFilter);
        return ResultUtil.success(b);
    }

    /**
     * 导出LDIF文件
     *
     * @param exportDto
     * @return
     */
    @Override
    public ResultVo<Boolean> exportLdifByBaseDn(LdifDto exportDto, HttpServletResponse response) {
        log.info("导出参数为:{}", exportDto);
        if (ObjectUtils.isEmpty(exportDto)
                || ObjectUtils.isEmpty(exportDto.getBaseDN())
                || ObjectUtils.isEmpty(exportDto.getScope())
                || ObjectUtils.isEmpty(exportDto.getExportType())
        ) {
            log.error("缺少参数:{}", exportDto);
            throw new SysException(ExceptionEnum.PARAM_ERROR);
        }
        //设置默认值
        exportDto.setBaseFilter(ldapSearchFilter);
        LdapTemplate newLdapTemplate = fromPool();
        Boolean result = LdapUtil.exportLdifFile(newLdapTemplate, exportDto, response);
        return ResultUtil.success(result);
    }

    /**
     * 导入LDIF文件
     *
     * @param importDto 上传的文件
     * @return 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> importLdifByBaseDn(ImportDto importDto) {
        log.info("恢复的方法参数为{}", importDto);
        //判断参数是否为空
        if (ObjectUtils.isEmpty(importDto)) {
            log.error("参数为空");
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        //获取上传的文件
        MultipartFile file = importDto.getFile();
        //判断上传文件为空
        if (file.isEmpty()) {
            log.error("文件为空");
            throw new SysException(ExceptionEnum.PARAM_ERROR);
        }
        //获取文件名称
        String name = file.getName();
        //对上传的文件进行解析

        LdapTemplate newLdapTemplate = fromPool();
        boolean result = LdapUtil.importLap(newLdapTemplate, file, name, importDto.getType());
        return ResultUtil.success(result);
    }

    /**
     * 新增LDAP节点
     *
     * @param createLdapDto 参数
     * @return true 成功 false 失败
     */
    @Override
    public ResultVo<Boolean> crateLdap(CreateLdapDto createLdapDto) {
        log.info("新增LDAP节点{}", createLdapDto);
        if (ObjectUtils.isEmpty(createLdapDto)
                || ObjectUtils.isEmpty(createLdapDto.getRdn())) {
            log.error("参数为空");
            throw new SysException(ExceptionEnum.PARAM_EMPTY);
        }
        LdapTemplate newLdapTemplate = fromPool();
        boolean result = LdapUtil.crateLdap(newLdapTemplate, createLdapDto);
        return ResultUtil.success(result);
    }

    @Override
    public Boolean exportQueryData(ParamDto paramDto, HttpServletResponse response) {
        String fileName = paramDto.getFileName();
        if (StaticValue.TRUE == paramDto.getWebOrFile()) {
            fileName = paramDto.getFileName() + LAST;
        } else {
            if (!fileName.endsWith(StaticValue.LDIF)) {
                fileName += StaticValue.LDIF;
            }
        }
        if (ObjectUtils.isEmpty(paramDto)) {
            return false;
        }
        //获取查询的数据
        List<String> writeData = queryData(paramDto);
        exportToLdif(writeData, fileName);
        if (paramDto.getWebOrFile().equals(StaticValue.FALSE)) {
            // 下载ldif文件
            Path file = Paths.get(fileName);
            if (Files.exists(file)) {
                response.setContentType("application/ldif");
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
                try {
                    Files.copy(file, response.getOutputStream());
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {

            Path file = Paths.get(fileName);
            //下载到服务器。判断文件是否存在 不存在就创建 不需要返回下的文件
            if (!Files.exists(file)) {
                //不存在
                try {
                    Files.createFile(file);
                    return true;
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new SysException(ExceptionEnum.FILE_IO_ERROR);
                }
            }
        }
        return true;
    }

    public List<String> queryData(ParamDto paramDto) {
        LdapTemplate newLdapTemplate = fromPool();
        List<CertTreeVo> listResultVo = LdapUtil.queryCertTree(newLdapTemplate, paramDto.getFilter(), paramDto.getBaseDN(),
                paramDto.getScope(), paramDto.getPageSize(), paramDto.getPage(), null);
        List<String> strings = new ArrayList<>();
        for (CertTreeVo certTreeVo : listResultVo) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            LdapTemplate ldapTemplateIn = fromPool();
            List<TreeVo> treeVos = LdapUtil.queryAttributeBytesInfo(ldapTemplateIn, certTreeVo.getRdn(), paramDto.isReturnAttr(), paramDto.getAttribute());
            map.put("dn:", certTreeVo.getRdn());
            strings.add("dn:" + certTreeVo.getRdn());
            for (TreeVo treeVo : treeVos) {
                map.put(treeVo.getKey(), treeVo.getValue());
                strings.add(treeVo.getKey() + ":" + treeVo.getValue());
            }
            strings.add(FEED);
        }
        return strings;
    }


    public static void exportToLdif(List<String> data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String line : data) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LdapTemplate fromPool() {
        // 从连接池获取PooledContextSource对象
        PooledContextSource pooledContextSource = (PooledContextSource) ldapTemplate.getContextSource();
// 将PooledContextSource转换为LdapContextSource
        LdapContextSource ldapContextSource = (LdapContextSource) pooledContextSource.getContextSource();
// 创建新的LdapTemplate对象
        LdapTemplate newLdapTemplate = new LdapTemplate(ldapContextSource);
        return newLdapTemplate;
    }

}
