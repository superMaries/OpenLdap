package cn.ldap.ldap.common.exception;


import cn.ldap.ldap.common.enums.ExceptionEnum;

/**
 * 全局异常处理.
 *
 * @author 李亮 2021-05-28
 */
public class SysException extends RuntimeException {

    private Integer code;

    public SysException(ExceptionEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }

    public SysException(ExceptionEnum resultEnum, String massage) {
        super(massage);
        this.code = resultEnum.getCode();
    }

    public SysException(String errMsg) {
        super(errMsg);
        this.code = ExceptionEnum.SYSTEM_ERROR.getCode();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

