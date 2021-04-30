package com.github.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 注解到接口上配置对应属性
 *
 * @author max
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RetrofitConfig {
    /**
     * @return key的名称。对应到配置中心
     */
    String value();

    /**
     * @return 描述
     */
    String desc() default "";
}