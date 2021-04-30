package com.github.rest.proxy.converter;

import com.github.rest.proxy.common.util.XmlParseUtils;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.reflect.Type;

public final class XmlResponseConverter<T> implements Converter<ResponseBody, T> {

    private final Type type;

    public XmlResponseConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String xmlStr = value.string();
        return XmlParseUtils.fromXml(xmlStr, (Class<T>) type);
    }
}
