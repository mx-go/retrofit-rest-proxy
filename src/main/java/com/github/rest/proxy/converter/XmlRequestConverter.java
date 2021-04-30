package com.github.rest.proxy.converter;

import com.github.rest.proxy.common.util.XmlParseUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.reflect.Type;

public final class XmlRequestConverter<T> implements Converter<T, RequestBody> {

    private static final MediaType XML_TEXT_TYPE = MediaType.parse("text/xml; charset=UTF-8");

    private final Type type;

    public XmlRequestConverter(Type type) {
        this.type = type;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        String xmlStr = XmlParseUtils.toXml(value);
        return RequestBody.create(XML_TEXT_TYPE, xmlStr);
    }
}
