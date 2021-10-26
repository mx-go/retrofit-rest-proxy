package com.github.rest.proxy.common;

import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 日志和header处理
 *
 * @author max
 */
public class LoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(Interceptor.class);

    private static final int LOG_MAX_LENGTH = 1024;

    @Override
    public Response intercept(Chain chain) throws IOException {
        long startTime = System.currentTimeMillis();
        Request request = chain.request();
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
            if (responseBodyStringInfo.length() > LOG_MAX_LENGTH) {
                responseBodyStringInfo = responseBodyStringInfo.substring(0, LOG_MAX_LENGTH) + "..more..";
            }
            if (requestHeadersStr.length() > LOG_MAX_LENGTH) {
                requestHeadersStr = requestHeadersStr.substring(0, LOG_MAX_LENGTH) + "..more..";
            }
            log.info("rest-proxy: execute {}ms. curl '{}' {} {}, response={}", elapsed, url, requestHeadersStr, requestBodyStr, responseBodyStringInfo);
            return response.newBuilder().body(ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes())).build();
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                long elapsed = System.currentTimeMillis() - startTime;
                String url = request.url().toString();
                String requestBodyStr = requestBodyToString(request);
                String requestHeadersStr = headersToString(request.headers());
                log.warn("rest-proxy: execute {}ms. curl '{}' {} {}, exception={}-{}", elapsed, url, requestHeadersStr, requestBodyStr, e.getClass().getSimpleName(), e.getMessage());
            }
            throw e;
        }
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
