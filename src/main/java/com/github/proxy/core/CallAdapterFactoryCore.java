package com.github.proxy.core;

import com.github.proxy.common.utils.Constants;
import com.github.proxy.common.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
public class CallAdapterFactoryCore extends CallAdapter.Factory {
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (returnType instanceof ParameterizedType) {
            Class clazz = getRawType(returnType);
            if (clazz == Call.class) {
                return null;
            }
        }
        return new CallAdapterFactoryCore.CallAdapterCore(returnType);
    }

    public static class CallAdapterCore<R> implements CallAdapter<R, R> {
        private Type returnType;

        public CallAdapterCore(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public R adapt(Call<R> call) {
            try {
                Response<R> response = call.execute();
                if (response.code() != Constants.SUCCESS) {
                    if (response.code() >= Constants.SERVER_ERROR) {
                        throw new IllegalStateException("Server internal error. response code!=200, code=" + response + ", msg=" + response.message());
                    }
                    throw new IllegalStateException("response code!=200, code=" + response + ", msg=" + response.message());
                }
                return response.body();
            } catch (Exception e) {

                ExceptionUtil.throwException(e);
                return null;
            }
        }

        private String formatParameters(Request request) {
            if (Constants.GET.equals(request.method())) {
                return request.url().encodedQuery();
            }
            StringBuilder sbd = new StringBuilder(128);
            HttpUrl url = request.url();
            if (url.encodedQuery() != null) {
                sbd.append(url.encodedQuery()).append('&');
            }
            RequestBody body = request.body();
            if (body instanceof FormBody) {
                FormBody form = (FormBody) body;
                for (int i = 0, len = form.size(); i < len; i++) {
                    String name = form.encodedName(i);
                    if ("pwd,password".contains(name)) {
                        sbd.append(name).append('=').append("...").append('&');
                    } else {
                        sbd.append(name).append('=').append(StringUtils.abbreviate(form.encodedValue(i), 64)).append('&');
                    }
                }
            }
            int length = sbd.length();
            if (length > 0 && sbd.charAt(length - 1) == '&') {
                sbd.setLength(length - 1);
            }
            return sbd.toString();
        }
    }
}