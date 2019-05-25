package com.github.proxy.common;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Random;

/**
 * 日志和header处理
 *
 * @author max
 */
@Slf4j
public class LoggingInterceptor implements Interceptor {

    /**
     * 签名需要加密的key
     */
    private String key;

    public LoggingInterceptor(String key) {
        this.key = key;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        long startTime = System.currentTimeMillis();
        Request request = addHeader(chain);
        try {
            Response response = chain.proceed(request);
            if (!log.isInfoEnabled()) {
                return response;
            }
            long elapsed = System.currentTimeMillis() - startTime;
            ResponseBody responseBody = response.body();
            String url = request.url().toString();
            String requestBodyStr = requestBodyToString(request);
            String requestHeadersStr = headersToString(request.headers());
            String responseBodyString = response.body().string();
            String responseBodyStringInfo = responseBodyString;
            // 避免打印过多返回日志
            if (responseBodyStringInfo.length() > 1024) {
                responseBodyStringInfo = responseBodyStringInfo.substring(0, 1024) + "..more..";
            }
            log.info("rest-api: execute {}ms, {}, headers={}, body={}, response={}", elapsed, url, requestHeadersStr,
                    requestBodyStr, responseBodyStringInfo);
            return response.newBuilder().body(ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes())).build();
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                String url = request.url().toString();
                String requestBodyStr = requestBodyToString(request);
                String requestHeadersStr = headersToString(request.headers());
                log.warn("rest-api:{}, headers={}, body={},exception={}-{}", url, requestHeadersStr, requestBodyStr,
                        e.getClass().getSimpleName(), e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 处理header头部
     *
     * @param chain
     * @return Request
     */
    private Request addHeader(Chain chain) {
        Request original = chain.request();
        /**
         * 是否需要加密
         */
        if (StringUtils.isBlank(key)) {
            return original;
        }
        String nonce = RandomStringUtils.randomNumeric(10);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = DigestUtils.md5Hex(nonce + timestamp + key);
        return original.newBuilder()
                .header("nonce", nonce)
                .header("timestamp", timestamp)
                .header("signature", signature)
                .method(original.method(), original.body())
                .build();
    }

    private String headersToString(Headers headers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(headers.name(i));
            builder.append(":");
            builder.append(headers.value(i));
        }
        return builder.toString();
    }

    private static String requestBodyToString(final Request request) throws IOException {
        if (request.body() == null) {
            return "";
        }
        final Request copy = request.newBuilder().build();
        final Buffer buffer = new Buffer();
        copy.body().writeTo(buffer);
        return buffer.readUtf8();
    }
}
