package com.github.rest.proxy.converter;

import com.github.rest.proxy.annotation.XmlRequest;
import com.github.rest.proxy.annotation.XmlResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 请求处理
 * <p>
 * Create by max on 2021/04/28
 **/
public final class DispatcherConverterFactory extends Converter.Factory {

    private static final MediaType STRING_MEDIA_TYPE = MediaType.parse("text/plain");

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (String.class.equals(type)) {
            return (Converter<ResponseBody, String>) ResponseBody::string;
        }
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == XmlResponse.class) {
                return new XmlResponseConverter<>(type);
            }
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (String.class.equals(type)) {
            return (Converter<String, RequestBody>) value -> RequestBody.create(STRING_MEDIA_TYPE, value);
        }
        for (Annotation annotation : methodAnnotations) {
            if (annotation.annotationType() == XmlRequest.class) {
                return new XmlRequestConverter<>(type);
            }
        }
        return null;
    }
}
