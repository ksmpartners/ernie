package com.ksmpartners.ernie.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.mockito.Mockito;
import org.testng.Assert;

/**
* Utility class used to test properties on objects.
*/
public class PropertyTestUtil {

    private final Object target;
    private final boolean mustHaveSetter;
    private final boolean mustHaveGetter;
    private final HashMap<Class<?>, Object> testValues = new HashMap<Class<?>, Object>();
    private final Class<?> clazz;

    /**
     * Constructs an instance of this test utility for the target object
     * specified.
     * <p>
     * @param target the Target object to test
     * @param mustHaveGetter flag indicating whether or not properties must have
     *           a getter
     * @param mustHaveSetter flag indicating whether or not properties must have
     *           a setter
     */
    public PropertyTestUtil(Object target, boolean mustHaveGetter, boolean mustHaveSetter) {
        this.target = target;
        this.mustHaveGetter = mustHaveGetter;
        this.mustHaveSetter = mustHaveSetter;
        this.createTestValues();
        this.clazz = target.getClass();
    }

    /**
     * The test values used in setting values.
     * <p>
     * @return a Map of Class and object instances to use when creating a set
     *         value
     */
    public HashMap<Class<?>, Object> getTestValues() {
        return testValues;
    }

    /**
     * Creates a standard hash of common test values for use with testing getters
     * and setters.
     */
    protected void createTestValues() {
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

    /**
     * Performs the getter/setter tests on the java object. The method works by
     * iterating over the declared fields (ignoring static fields) and ensures
     * that each field has a getter and setter (as indicated by the parameters)
     * and that the results of calling the setter followed by the getter match.
     * <p>
     */
    public void testGettersSetters() {

        PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(target);
        for (PropertyDescriptor property : properties) {

            final String qName = "[" + clazz.getName() + "]." + property.getName();
            final Class<?> fieldType = property.getPropertyType();

            // ignore getClass()
            if (property.getName().equals("class"))
                continue;

            // create a test value for our setter
            Object setValue = createSetValue(qName, fieldType);
            this.testProperty(property, setValue, qName);
        }
    }

    /**
     * Test setter and getter call and make sure they match.
     * <p>
     * @param property the property to be set
     * @param setValue the value to be set
     * @param qName qualified name for error reporting
     */
    private void testProperty(PropertyDescriptor property, Object setValue, String qName) {

        // flag indicating whether a comparison should be done at the end
        boolean expectMatch = true;

        // call setter (if exists)
        if (property.getWriteMethod() != null) {
            invokeMethod(target, property.getWriteMethod(), new Object[] { setValue }, qName);
        } else {
            Assert.assertFalse(mustHaveSetter, "Property " + qName + " does not have the required setter.");
            expectMatch = false;
        }

        // call getter (if exists)
        Object getValue = null;
        if (property.getReadMethod() != null) {
            getValue = invokeMethod(target, property.getReadMethod(), null, qName);
        } else {
            Assert.assertFalse(mustHaveGetter, "Property " + qName + " does not have the required getter.");
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
    private Object createSetValue(final String qName, final Class<?> fieldType) {
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

}