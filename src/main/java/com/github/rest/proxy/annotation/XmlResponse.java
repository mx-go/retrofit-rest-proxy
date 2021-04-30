package com.github.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * XML响应
 * <p>
 * Create by max on 2021/04/30
 **/
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XmlResponse {
}