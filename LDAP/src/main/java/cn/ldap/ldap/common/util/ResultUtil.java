package cn.ldap.ldap.common.util;



import cn.ldap.ldap.common.vo.PageVo;
import cn.ldap.ldap.common.vo.ResultVo;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: ResultUtil
 * @Author Wy
 * @Date: 2022/8/31 10:55
 * @Version 1.0
 */
public class ResultUtil {

    public static ResultVo success(Object o) {
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SUCCESS.getCode());
        vo.setMessage(ResultEnum.SUCCESS.getMessage());
        vo.setData(o);
        return vo;
    }

    public static ResultVo success(PageVo pageVo) {
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SUCCESS.getCode());
        vo.setMessage(ResultEnum.SUCCESS.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put("pageIndex", pageVo.pageIndex);
        map.put("pageSize", pageVo.pageSize);
        map.put("total", pageVo.total);
        map.put("list", pageVo.list);
        vo.setData(map);
        return vo;
    }

    public static ResultVo success() {
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SUCCESS.getCode());
        vo.setMessage(ResultEnum.SUCCESS.getMessage());
        return vo;
    }

    public static ResultVo success(String massage) {
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SUCCESS.getCode());
        vo.setMessage(massage);
        return vo;
    }

    /**
     * 更加code message返回
     *
     * @param code    状态码
     * @param massage  提示信息
     * @return
     */
    public static ResultVo success(String code, String massage) {
        ResultVo vo = new ResultVo();
        vo.setCode(code);
        vo.setMessage(massage);
        return vo;
    }
    public static  ResultVo fail(){
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SYSTEM_ERROR.getCode());
        vo.setMessage(ResultEnum.SYSTEM_ERROR.getMessage());
        return vo;
    }
    public static ResultVo fail(String code, String massage) {
        ResultVo vo = new ResultVo();
        vo.setCode(code);
        vo.setMessage(massage);
        return vo;
    }

    public static ResultVo fail(ResultEnum result) {
        ResultVo vo = new ResultVo();
        vo.setCode(result.getCode());
        vo.setMessage(result.getMessage());
        return vo;
    }
    public static ResultVo fail(ResultEnum result,String  message) {
        ResultVo vo = new ResultVo();
        vo.setCode(result.getCode());
        vo.setMessage(message);
        return vo;
    }
    public static ResultVo fail( String  message) {
        ResultVo vo = new ResultVo();
        vo.setCode(ResultEnum.SYSTEM_ERROR.getCode());
        vo.setMessage(message);
        return vo;
    }

}
