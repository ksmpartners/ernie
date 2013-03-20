/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksmpartners.ernie.model.JobStatus;
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

        String newJson = serialize(newT);

        // TODO: Verify that this is a good test.
        Assert.assertTrue(json.equals(newJson), "Pre and post serialization JSONs are not equal for class " + clazz.getCanonicalName());
    }

    /**
     * Checks equality of two objects based on the equality of their fields, items, or .equals() method.
     * @param obj1
     * @param obj2
     * @return
     */
    public static <T> boolean equal(T obj1, T obj2)
    {
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

        List<Pair> vals = new ArrayList<Pair>();

        // If obj1 and obj2 are Collections, get the objects in them
        if(Collection.class.isAssignableFrom(obj1.getClass())) {

            Collection c1 = (Collection) obj1;
            Collection c2 = (Collection) obj2;

            if(c1.size()!=c2.size())
                return false;

            Iterator itr1 = c1.iterator();
            Iterator itr2 = c2.iterator();

            while(itr1.hasNext() && itr2.hasNext()) {
                vals.add(new Pair(itr1.next(), itr2.next()));
            }

        }

        // Get field values from obj1 and obj2
        PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(obj1);
        for (PropertyDescriptor property : properties) {

            // ignore getClass() and isEmpty()
            if (property.getName().equals("class") || property.getName().equals("empty"))
                continue;

            Object val1 = invokeMethod(obj1, property.getReadMethod(), null, property.getName());
            Object val2 = invokeMethod(obj2, property.getReadMethod(), null, property.getName());

            vals.add(new Pair(val1, val2));
        }

        if(vals.isEmpty())
            return false;

        for(Pair pair : vals) {
            if(!equal(pair.left, pair.right))
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
        testValues.put(JobStatus.class, JobStatus.PENDING);
    }

    private static class Pair {
        Object left;
        Object right;

        public Pair(Object left, Object right)
        {
            this.right = right;
            this.left = left;
        }
    }

}
