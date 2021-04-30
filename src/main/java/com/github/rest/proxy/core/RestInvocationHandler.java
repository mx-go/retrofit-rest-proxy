package com.github.rest.proxy.core;

import com.github.rest.proxy.HttpConfig;
import com.github.rest.proxy.common.LoggingInterceptor;
import com.github.rest.proxy.common.util.UrlUtils;
import com.github.rest.proxy.converter.DispatcherConverterFactory;
import com.google.gson.Gson;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * 代理对象
 * <p>
 * Create by max on 2021/04/29
 **/
public class RestInvocationHandler<T> implements InvocationHandler {

    private volatile T target;
    private final Class<T> clazz;
    private final Gson gson;

    public RestInvocationHandler(Class<T> clazz, Gson gson) {
        this.clazz = clazz;
        this.gson = gson;
    }

    public void initOrReload(HttpConfig.Config config) {
        Retrofit retrofit = newRetrofit(config);
        this.target = retrofit.create(clazz);
    }

    private Retrofit newRetrofit(HttpConfig.Config config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        int readTimeout = 5000;
        int connectTimeout = 5000;
        if (config.getReadTimeout() != null) {
            readTimeout = config.getReadTimeout();
        }
        if (config.getConnectTimeout() != null) {
            connectTimeout = config.getConnectTimeout();
        }
        if (config.getProxy() != null) {
            Pair<String, Integer> pair = config.getProxy();
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pair.getLeft(), pair.getRight())));
        }
        if (config.getUserName() != null && config.getPassword() != null) {
            builder.authenticator((route, response) -> response.request().newBuilder()
                    .header("Authorization", Credentials.basic(config.getUserName(), config.getPassword()))
                    .build());
        }

        builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        builder.addInterceptor(new LoggingInterceptor());
        OkHttpClient okHttpClient = builder.build();
        String baseUrl = UrlUtils.getServiceUrl(config.getDomain());

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new DispatcherConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(new CallAdapterFactoryCore())
                .client(okHttpClient)
                .build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
