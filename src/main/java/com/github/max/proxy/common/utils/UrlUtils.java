package com.github.max.proxy.common.utils;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;

/**
 * @author max
 */
@UtilityClass
public class UrlUtils {
    /**
     * 简单的添加url的操作
     * 若url中以/结尾,否则添加至尾部
     * 若url中开头是 http 就不添加前缀，若没有则添加 http://
     *
     * @param baseUrl 基础url
     * @return url
     */
    public static String getServiceUrl(String baseUrl) {
        // Retrofit2以/结尾
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        // 添加http前缀
        if (Strings.isNullOrEmpty(baseUrl) || baseUrl.startsWith("http")) {
            return baseUrl;
        }
        return "http://" + baseUrl;
    }
}