package com.ksmpartners.ernie.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.mockito.Mockito;
import org.testng.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * Utility for testing Jackson serialization
 */
public class TestUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HashMap<Class<?>, Object> testValues = new HashMap<Class<?>, Object>();

    /**
     * Serializes an object to a JSON String
     * @param obj - the Object to be serialized
     * @return - the resulting JSON as a String
     */
    public static String serialize(Object obj)
    {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize object", e);
        }
    }

    /**
     * Deserializes the given JSON String into an object of type clazz
     * @param json - the JSON String to be deserialized
     * @param clazz - the Class of the resulting object
     * @return - the deserialized object
     */
    public static <T> T deserialize(String json, Class<T> clazz)
    {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize object", e);
        }
    }

    /**
     * Verifies that an object created from the given Class can be serialized to/from a JSON and the resulting object
     * is equal to the original.
     * @param clazz - the Class to be verified
     * @throws InstantiationException, IllegalAccessException - if the object can't be instantiated
     */
    public static <T> void verifySerialization(Class<T> clazz)
        throws InstantiationException, IllegalAccessException
    {
        T t = clazz.newInstance();

        populateFields(clazz, t);

        String json = serialize(t);

        T newT = deserialize(json, clazz);

        Assert.assertTrue(equal(t,newT), "Pre and post serialization objects are not equal for class " + clazz.getCanonicalName());
    }

    /**
     * Checks equality of two objects based on the equality of their fields.
     * @param obj1
     * @param obj2
     * @return
     */
    public static <T> boolean equal(T obj1, T obj2)
    {
        Map<String, Method> methodNameToMethodMap = new HashMap<String, Method>();

        if(obj1 == null || obj2 == null) {
            // If they're both null, we call this equal
            if(obj1 == null && obj2 == null)
                return true;
            else
                return false;
        }

        if(!obj1.getClass().equals(obj2.getClass()))
            return false;

        if(obj1.equals(obj2))
            return true;

        // Assemble the map of field name to getter method
        PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(obj1);
        for (PropertyDescriptor property : properties) {

            // ignore getClass() and isEmpty()
            if (property.getName().equals("class") || property.getName().equals("empty"))
                continue;

            methodNameToMethodMap.put(property.getName(),property.getReadMethod());
        }

        // If there are no fields and obj1.equals(obj2) returned false, then they're not equal
        if(methodNameToMethodMap.isEmpty())
            return false;

        // Get the field values and compare them for each object
        for(String prop : methodNameToMethodMap.keySet()) {
            Method currMethod = methodNameToMethodMap.get(prop);
            Object val1 = invokeMethod(obj1, currMethod, null, prop);
            Object val2 = invokeMethod(obj2, currMethod, null, prop);

            if(!equal(val1, val2))
                return false;
        }

        return true;
    }

    /**
     * Performs the getter/setter tests on the java object. The method works by
     * iterating over the declared fields (ignoring static fields) and ensures
     * that each field has a getter and setter (as indicated by the parameters)
     * and that the results of calling the setter followed by the getter match.
     * <p>
     */
    public static void populateFields(Class clazz, Object target) {

        PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(target);
        for (PropertyDescriptor property : properties) {

            final String qName = "[" + clazz.getName() + "]." + property.getName();
            final Class<?> fieldType = property.getPropertyType();

            // ignore getClass() and isEmpty()
            if (property.getName().equals("class") || property.getName().equals("empty"))
                continue;

            // create a test value for our setter
            Object setValue = createSetValue(qName, fieldType);
            testProperty(target, property, setValue, qName);
        }
    }

    /**
     * Test setter and getter call and make sure they match.
     * <p>
     * @param property the property to be set
     * @param setValue the value to be set
     * @param qName qualified name for error reporting
     */
    private static void testProperty(Object target, PropertyDescriptor property, Object setValue, String qName) {

        // flag indicating whether a comparison should be done at the end
        boolean expectMatch = true;

        // call setter (if exists)
        if (property.getWriteMethod() != null) {
            invokeMethod(target, property.getWriteMethod(), new Object[] { setValue }, qName);
        } else {
            Assert.assertFalse(true, "Property " + qName + " does not have the required setter.");
            expectMatch = false;
        }

        // call getter (if exists)
        Object getValue = null;
        if (property.getReadMethod() != null) {
            getValue = invokeMethod(target, property.getReadMethod(), null, qName);
        } else {
            Assert.assertFalse(true, "Property " + qName + " does not have the required getter.");
            expectMatch = false;
        }

        // if expecting a match, compare
        // if they are not the same instance, assert that they have equality
        if (expectMatch && setValue != getValue)
            Assert.assertEquals(getValue, setValue,
                    "Values did not match for getter/setter call on field " + qName);
    }

    /**
     * Invoke the specified method and translate failures into assertion
     * failures.
     * <p>
     * @param target the target object
     * @param method the Method to invoke
     * @param value the value to pass in
     * @param qName a qualified property name for reporting failures
     * @return the object value returned if any
     */
    private static Object invokeMethod(Object target, Method method, Object[] value, String qName) {
        try {
            Object result = method.invoke(target, value);
            return result;
        } catch (Exception e) {
            Assert.fail("Failed invoking method " + method.getName() + " for property " + qName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a test value to be used for a setter call. If primitive, it will
     * use Mockito. If not, will check the testValues hash specified for
     * pre-wired classes. If not found, will create an empty instance.
     * <p>
     * @param qName the qualified name of the property in the event of failure
     * @param fieldType the target field type
     * @return a new value to be used in setter calls
     */
    private static Object createSetValue(final String qName, final Class<?> fieldType) {
        createTestValuesIfNeeded();
        // create a set value
        Object setValue = null;
        try {
            if (fieldType.isPrimitive())
                setValue = Mockito.any(fieldType);
            else {
                // look to see if we have a mock object already for this type
                setValue = testValues.get(fieldType);

                // if still null, create instance
                if (setValue == null)
                    setValue = fieldType.newInstance();
            }
        } catch (Exception e) {
            Assert.fail("Unable to mock parameter of type [" + fieldType.getName() + "] for field "
                    + qName + ": " + e.getMessage());
        }
        return setValue;
    }

    private static void createTestValuesIfNeeded()
    {
        if(testValues.isEmpty())
            createTestValues();
    }

    /**
     * Creates a standard hash of common test values for use with testing getters
     * and setters.
     */
    private static void createTestValues() {
        testValues.put(Long.class, Long.valueOf(123L));
        testValues.put(Integer.class, Integer.valueOf(456));
        testValues.put(String.class, "FOOBAR");
        testValues.put(Double.class, Double.valueOf(123.456));
        testValues.put(Float.class, Float.valueOf(789.123f));
        testValues.put(BigDecimal.class, BigDecimal.ZERO);
        testValues.put(Boolean.class, Boolean.TRUE);
        testValues.put(Byte.class, Mockito.anyByte());
        testValues.put(Character.class, Mockito.anyChar());
        testValues.put(Collection.class, Mockito.anyCollection());
        testValues.put(List.class, Mockito.anyList());
        testValues.put(Set.class, Mockito.anySet());
        testValues.put(Map.class, Mockito.anyMap());
    }

}
