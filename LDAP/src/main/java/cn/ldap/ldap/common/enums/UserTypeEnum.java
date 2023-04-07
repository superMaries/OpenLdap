package cn.ldap.ldap.common.enums;


public enum UserTypeEnum {

    USER_ADMIN(1, "管理員"),
    USER_AUDITOR(2, "审计员"),
    ;

    private Integer code;

    private String name;

    /**
     *
     * @param code
     * @param name
     */
    UserTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }


    /**
     * 获取对应的提code
     * @return
     */
    public Integer getCode(){
        return  code;
    }

    /**
     * 获取对应的提示信息
     * @return
     */
    public String  getName(){
        return name;
    }

    /**
     * 根据code 获取提示信息
     * @param code
     * @return
     */
    public String getName(Integer code){
        for (UserTypeEnum uEnum:UserTypeEnum.values()) {
            if (uEnum.code.equals(code)){
                return uEnum.name;
            }
        }
        return "";
    }
}
