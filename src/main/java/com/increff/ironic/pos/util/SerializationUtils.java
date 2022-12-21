package com.increff.ironic.pos.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SerializationUtils {

    static public Map<String, Object> getAttributeMap(Object object) {
        Class<?> clazz = object.getClass();
        Field[] attributes = clazz.getDeclaredFields();

        Map<String, Object> attributeMap = new HashMap<>();

        for (Field field : attributes) {
            // Getting field name
            String attributeName = field.getName();

            // Constructing getter method name
            String getterMethodName = "get"
                    + attributeName.substring(0, 1).toUpperCase()
                    + attributeName.substring(1);

            Method getMethod;
            try {
                getMethod = clazz.getMethod(getterMethodName);
                Object valObject = getMethod.invoke(object);
                attributeMap.put(attributeName, valObject);
            } catch (Exception ignored) {
            }
        }

        return attributeMap;
    }

}
