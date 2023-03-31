package cn.ldap.ldap.common.aop.annotations;

import cn.ldap.ldap.common.enums.OperateMenuEnum;
import cn.ldap.ldap.common.enums.OperateTypeEnum;

import java.lang.annotation.*;

/**
 * @title: OperateAnnotaion
 * @Author Wy
 * @Date: 2023/3/31 15:38
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface OperateAnnotation {
    /**
     * 操作模块
     *
     */
    OperateMenuEnum operateModel();

    /**
     * 操作类型
     */
    OperateTypeEnum operateType();
    /**
     * 备注.
     */
    String remark() default "";
}
