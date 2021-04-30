package com.github.rest.proxy.common.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class XmlParseUtils {

    private XmlParseUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String strXml, Class<T> clazz) {
        XStream xStream = new XStream();
        xStream.processAnnotations(clazz);
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.ignoreUnknownElements();
        T result = (T) xStream.fromXML(strXml);
        if (result instanceof AllPropertiesCollector) {
            AllPropertiesCollector allPropertiesCollector = (AllPropertiesCollector) result;
            String rootName = clazz.getAnnotation(XStreamAlias.class) != null ? clazz.getAnnotation(XStreamAlias.class).value() : "xml";
            XStream mapXStream = new XStream();
            mapXStream.registerConverter(new MapEntryConverter());
            mapXStream.alias(rootName, Map.class);
            allPropertiesCollector.allProperties = (Map<String, String>) mapXStream.fromXML(strXml, Map.class);
        }
        return result;
    }

    public static <T> String toXml(T bean) {
        XStream xStream = new XStream(new DomDriver("UTF_8", new NoNameCoder()));
        xStream.processAnnotations(bean.getClass());
        xStream.setMode(XStream.NO_REFERENCES);
        return xStream.toXML(bean);
    }

    public static class AllPropertiesCollector {
        private Map<String, String> allProperties = new HashMap<>();

        public Map<String, String> getAllProperties() {
            return allProperties;
        }

        public void setAllProperties(Map<String, String> allProperties) {
            this.allProperties = allProperties;
        }
    }

    public static class MapEntryConverter implements Converter {

        @Override
        public boolean canConvert(Class clazz) {
            return AbstractMap.class.isAssignableFrom(clazz);
        }

        @Override
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

            AbstractMap map = (AbstractMap) value;
            for (Object obj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                writer.startNode(entry.getKey().toString());
                Object val = entry.getValue();
                if (null != val) {
                    writer.setValue(val.toString());
                }
                writer.endNode();
            }
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            Map<String, String> map = new HashMap<>(16);
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String key = reader.getNodeName(); // nodeName aka element's name
                String value = reader.getValue();
                map.put(key, value);
                reader.moveUp();
            }
            return map;
        }
    }
}
