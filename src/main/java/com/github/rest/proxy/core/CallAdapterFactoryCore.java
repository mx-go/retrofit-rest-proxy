package com.github.rest.proxy.core;

import com.github.rest.proxy.common.FlexibleConfig;
import com.github.rest.proxy.common.RetrofitCallable;
import com.github.rest.proxy.common.util.Constants;
import com.github.rest.proxy.common.util.ExceptionUtils;
import com.github.rholder.retry.Retryer;
import com.google.common.base.Joiner;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class CallAdapterFactoryCore extends CallAdapter.Factory {

    private static final Logger log = LoggerFactory.getLogger(CallAdapterFactoryCore.class);

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (returnType instanceof ParameterizedType) {
            Class<?> clazz = getRawType(returnType);
            if (clazz == Call.class) {
                return null;
            }
        }
        return new CallAdapterCore<>(returnType);
    }

    @SuppressWarnings("unchecked")
    public static class CallAdapterCore<R> implements CallAdapter<R, R> {

        private final Type returnType;

        public CallAdapterCore(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public R adapt(Call<R> call) {
            FlexibleConfig<R> flexible = getFlexible(call);
            RetrofitCallable<R> retrofitCallable = flexible.getCallBackClazz();
            Retryer<Response<R>> retryer = flexible.getRetryer();

            Request request = call.request();
            try {
                Callable<Response<R>> callable = () -> {
                    Response<R> r;
                    try {
                        r = call.clone().execute();
                    } catch (Exception e) {
                        // retry when read timeout
                        okhttp3.Response response = new okhttp3.Response.Builder()
                                .code(Constants.CALL_EXCEPTION)
                                .message(e.getClass().getSimpleName())
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url(request.url()).build())
                                .build();
                        r = Response.error(ResponseBody.create(getMediaType(request), org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e)), response);
                    }

                    if (r.code() != Constants.SUCCESS) {
                        String msg;
                        if (r.code() >= Constants.SERVER_ERROR) {
                            msg = String.format("Server internal error. response code!=200. code=%s, msg=%s", r, r.errorBody().string());
                        } else {
                            msg = String.format("Response code!=200. code=%s, msg=%s", r, r.errorBody().string());
                        }
                        log.error(msg);
                        retrofitCallable.after(request, r, new IllegalStateException(msg));
                        return r;
                    }
                    retrofitCallable.after(request, r, null);
                    return r;
                };
                return retryer.call(callable).body();
            } catch (Exception e) {
                retrofitCallable.after(request, null, e);
                ExceptionUtils.throwException(e);
                return null;
            }
        }

        private MediaType getMediaType(Request request) {
            if (request.body() != null) {
                return ObjectUtils.defaultIfNull(request.body().contentType(), MEDIA_TYPE);
            }
            return MEDIA_TYPE;
        }

        private FlexibleConfig<R> getFlexible(Call<R> call) {
            Method method = call.request().tag(Invocation.class).method();
            String clazzName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            String key = Joiner.on(".").join(clazzName, methodName);
            return ConfigRetrofitSpringFactory.flexibleMap.getOrDefault(key, defaultFlexible());
        }

        private FlexibleConfig<R> defaultFlexible() {
            return new FlexibleConfig<R>().defaultConfig();
        }
    }
}