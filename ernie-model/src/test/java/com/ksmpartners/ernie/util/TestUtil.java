package com.ksmpartners.ernie.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static String serialize(Object obj)
    {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize object", e);
        }
    }

    public static <T> T deserialize(String json, Class<T> clazz)
    {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize object", e);
        }
    }

    public static <T> void verifySerialization(Class<T> clazz)
        throws InstantiationException, IllegalAccessException
    {
        T t = clazz.newInstance();

        PropertyTestUtil testUtil = new PropertyTestUtil(t, true, true);

        testUtil.testGettersSetters();

        String json = serialize(t);

        T newT = deserialize(json, clazz);

        if(!t.equals(newT))
            throw new RuntimeException("Could not verify serialization of " + clazz.getCanonicalName());
    }

}
