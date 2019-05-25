package com.github.proxy;

import com.github.proxy.common.utils.Constants;
import com.github.proxy.common.utils.ExceptionUtil;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author max
 */
public class CustomCallAdapterFactory extends CallAdapter.Factory {
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (returnType instanceof ParameterizedType) {
            Class clazz = getRawType(returnType);
            if (clazz == Call.class) {
                return null;
            }
        }
        return new CustomCallAdapter(returnType);
    }

    public static class CustomCallAdapter<R> implements CallAdapter<R, R> {
        private Type returnType;

        CustomCallAdapter(Type returnType) {
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
                    throw new IllegalStateException("response code!=200, code=" + response + ", msg=" + response.message());
                }
                return response.body();
            } catch (Exception e) {
                ExceptionUtil.throwException(e);
                return null;
            }
        }
    }
}
