//package cn.ldap.ldap.common.entity.ldap;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import org.springframework.ldap.odm.annotations.Attribute;
//import org.springframework.ldap.odm.annotations.Entry;
//import org.springframework.ldap.odm.annotations.Id;
//import org.springframework.ldap.support.LdapNameBuilder;
//
//import javax.naming.Name;
//
///**
// * @title: Person
// * @Author Wy
// * @Date: 2023/4/3 13:56
// * @Version 1.0
// */
////@Entry(objectClasses = {"organizationalPerson","person","top"},base = "o=myorg")
//@Entry(base = "cn=xxx xx,dc=test,dc=com", objectClasses = "posixAccount")
//public class Person {
//    @Id
//    @JsonIgnore
//    private Name dn;
//
//    @Attribute(name="cn")
//    private String cn;
//
//    @Attribute(name="sn")
//    private String sn;
//
//    @Attribute(name="userPassword")
//    private String userPassword;
//
//    public Person(String cn) {
//        Name dn = LdapNameBuilder.newInstance()
//                .add("o", "myorg")
//                .add("cn", cn)
//                .build();
//        this.dn = dn;
//    }
//    public Person(){}
//
//    /* getter */
//    public Name getDn() {
//        return dn;
//    }
//
//    public String getCn() {
//        return cn;
//    }
//
//    public String getSn() {
//        return sn;
//    }
//
//    public String getUserPassword() {
//        return userPassword;
//    }
//
//    /* setter */
//    public void setDn(Name dn) {
//        this.dn = dn;
//    }
//
//    public void setCn(String cn) {
//        this.cn = cn;
//        if(this.dn==null){
//            Name dn = LdapNameBuilder.newInstance()
//                    .add("o", "myorg")
//                    .add("cn", cn)
//                    .build();
//            this.dn = dn;
//        }
//    }
//
//    public void setSn(String sn) {
//        this.sn = sn;
//    }
//
//    public void setUserPassword(String userPassword) {
//        this.userPassword = userPassword;
//    }
//
//    @Override
//    public String toString() {
//        return "Person{" +
//                "dn=" + dn.toString() +
//                ", cn='" + cn + '\'' +
//                ", sn='" + sn + '\'' +
//                ", userPassword='" + userPassword + '\'' +
//                '}';
//    }
//}
