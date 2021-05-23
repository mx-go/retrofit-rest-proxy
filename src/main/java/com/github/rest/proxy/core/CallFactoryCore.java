package com.github.rest.proxy.core;

import com.github.rest.proxy.common.FlexibleConfig;
import com.github.rest.proxy.common.Header;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Request;
import retrofit2.Invocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 请求处理
 * <p>
 * Create by max on 2021/05/22
 **/
public class CallFactoryCore implements Call.Factory {

    /**
     * 自定义请求头
     */
    private final Map<String, String> headers;
    private final Call.Factory delegate;

    public CallFactoryCore(Map<String, String> headers, Call.Factory delegate) {
        this.headers = headers;
        this.delegate = delegate;
    }

    @Override
    public Call newCall(Request request) {
        // 添加配置中心header
        Request.Builder builder = request.newBuilder();
        headers.forEach(builder::addHeader);

        List<?> arguments = Objects.requireNonNull(request.tag(Invocation.class)).arguments();
        Header header = getHeaderClazz(Objects.requireNonNull(request.tag(Invocation.class)).method());
        // 添加生成的header
        Headers headers = builder.build().headers();
        Map<String, String> headerMap = header.getHeader(headers, arguments);
        if (headerMap != null && !headerMap.isEmpty()) {
            headerMap.forEach(builder::addHeader);
            request = builder.build();
        }
        return delegate.newCall(request);
    }

    private Header getHeaderClazz(Method method) {
        return FlexibleConfig.getFlexible(method).getHeader();
    }
}