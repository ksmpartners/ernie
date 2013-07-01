package com.ksmpartners.ernie.util;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.*;

/**
 * Test class for MultiValueHashMap
 *
 * @author tmulle
 * @version $Revision: #1 $
 */
public class MultiValueHashMapTest {


    private static final String HELLO_KEY = "Hello";
    private static final String KEY1_KEY = "Key1";
    private static final String VALUE1_VALUE = "Value1";
    private static final String KEY2_KEY = "Key2";
    private static final String MULTIVALUE_KEY = "MultiValue";
    private static final String VALUE2_VALUE = "Value2";
    private static final String VALUE3_VALUE = "Value3";
    private static final String WORLD_VALUE = "World";


    @Test
    public void testMap() {

        Map m = getDefaultMap();
        testMapValues(m);
        testMapSize(m, 3);

        m = getCapacityMap();
        testMapValues(m);
        testMapSize(m, 3);


        // Test prepopped values
        m = getPrepoppedMap();
        testPrePoppedMapValues(m);
        testMapSize(m, 4);
    }

    public void testPrePoppedMapValues(Map map) {

        assertNotNull(map);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(HELLO_KEY));

        testMapValues(map);
    }


    private void testMapValues(Map map) {

        assertNotNull(map);

        // Regular single keys
        map.put(KEY1_KEY, VALUE1_VALUE);
        map.put(KEY2_KEY, new Integer(1234));

        // Multi values for keys
        map.put(MULTIVALUE_KEY, VALUE1_VALUE);
        map.put(MULTIVALUE_KEY, VALUE2_VALUE);
        map.put(MULTIVALUE_KEY, VALUE3_VALUE);

        // Check the values in the map now
        List values = (List) map.get(KEY1_KEY);
        assertNotNull(values);
        assertEquals(VALUE1_VALUE, values.get(0));

        values = (List) map.get(KEY2_KEY);
        assertNotNull(values);
        assertEquals(new Integer(1234), values.get(0));

        // Check multivalues
        values = (List) map.get(MULTIVALUE_KEY);

        assertEquals("List should have contained 3 items", 3, values.size());
        assertTrue(values.contains(VALUE1_VALUE));
        assertTrue(values.contains(VALUE2_VALUE));
        assertTrue(values.contains(VALUE3_VALUE));
    }

    private void testMapSize(Map map, int size) {
        assertEquals("Map should've had " + size + " entries", size, map.size());
    }


    // Privates
    private Map getDefaultMap() {
        return new MultiValueHashMap();
    }

    private Map getCapacityMap() {
        return new MultiValueHashMap(3);
    }

    private Map getPrepoppedMap() {
        Map m = new HashMap();
        m.put(HELLO_KEY, WORLD_VALUE);

        return new MultiValueHashMap(m);
    }

}
