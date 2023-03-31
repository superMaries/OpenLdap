package cn.ldap.ldap.common.enums;


public enum UserTypeEnum {

    USER_ADMIN(1, "管理員"),
    USER_AUDITOR(2, "审计员"),
    ;

    private Integer code;

    private String name;

    UserTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }


    public Integer getCode(){
        return  code;
    }

    public String  getName(){
        return name;
    }
    public String getName(Integer code){
        for (UserTypeEnum uEnum:UserTypeEnum.values()) {
            if (uEnum.code.equals(code)){
                return uEnum.name;
            }
        }
        return "";
    }
}
