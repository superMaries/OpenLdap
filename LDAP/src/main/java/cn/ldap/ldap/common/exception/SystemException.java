package cn.ldap.ldap.common.exception;

import cn.ldap.ldap.common.enums.ExceptionEnum;

public class SystemException extends RuntimeException{

    private Integer code;

    public SystemException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.code = exceptionEnum.getCode();
    }

    public SystemException(ExceptionEnum exceptionEnum,String massage) {
        super(massage);
        this.code = exceptionEnum.getCode();
    }

    public SystemException(String errMsg) {
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
