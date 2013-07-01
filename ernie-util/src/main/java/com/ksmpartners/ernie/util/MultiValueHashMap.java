package com.ksmpartners.ernie.util;

import java.util.*;


/**
 * Class that extends HashMap and provides a mutlivalued
 * HashMap functionality.
 * <p/>
 * This is a utility class that will allow you to stored multiple values
 * for the same key automatically.
 * <p/>
 * See code below:
 * <p/>
 * <CODE>
 * MultiValueHashMap table = new MultiValueHashMap();
 * <p/>
 * // This will store a single value for the key
 * table.put("SINGLE", "ITEM 1");
 * <p/>
 * // This will store multiple values for the key
 * table.put("MULTI", "ITEM 1");
 * table.put("MULTI", "ITEM 2");
 * table.put("MULTI", "ITEM 3");
 * table.put("MULTI", "ITEM 4");
 * </CODE>
 *
 * @author tmulle
 * @version $Revision: #1 $
 * @see java.util.HashMap
 */
public class MultiValueHashMap extends HashMap {

    /**
     * Default constructor
     */
    public MultiValueHashMap() {
        super();
    }

    /**
     * Constructor that takes the initialCapacity
     *
     * @param initialCapacity Initial Capacity of the table
     */
    public MultiValueHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor that takes the initialCapacity and loadFactor
     *
     * @param initialCapacity Initial Capacity of the table
     * @param loadFactor      Load Factor of the table
     */
    public MultiValueHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructor that takes a map of values
     * <p/>
     * This method will automatically create the multivalued value entries
     *
     * @param map Map of values to initialize the table with
     */
    public MultiValueHashMap(Map map) {
        super(map);
    }

    /**
     * Method to return if the tables contains the specified value
     * <p/>
     * This potentially can be expensive if there are a large number of
     * values stored for a single key
     *
     * @param value Value to look for
     * @return True if found, false otherwise
     */
    public boolean containsValue(Object value) {

        if (value == null)
            throw new NullPointerException();

        List values;
        Iterator itr = values().iterator();

        while (itr.hasNext()) {

            values = (List)itr.next();

            if (values != null) {

                // If we are passed a List object
                if (values.equals(value))
                    return true;

                // Else search all of the multivalued lists for the value for
                // the first occurence
                if (values.contains(value))
                    return true;
            }
        }

        return false;
    }

    /**
     * Method to store the value under the specified key
     *
     * @param key   Key to store the value under
     * @param value Object to store
     * @return Previous object stored under this key
     */
    public Object put(Object key, Object value) {

        // Store the values in a ArrayList by default
        return put(key, value, ArrayList.class, -1);
    }

    /**
     * Method to store the value under the specified key
     *
     * @param key   Key to store the value under
     * @param value Object to store
     * @param index Index into the list of values to place the new value
     * @return Previous object stored under this key
     */
    public Object put(Object key, Object value, int index) {

        // Store the values in a ArrayList by default
        return put(key, value, ArrayList.class, index);
    }

    /**
     * Method to store the value under the specified key appending the
     * value to the end of the list
     *
     * @param key      Key to store the value under
     * @param value    Object to store
     * @param listType Type of List implementation to use to store multivalues in
     * @return Previous object stored under this key
     */
    public Object put(Object key, Object value, Class listType) {

        return put(key, value, ArrayList.class, -1);
    }

    /**
     * Method to store the value under the specified key
     *
     * @param key      Key to store the value under
     * @param value    Object to store
     * @param listType Type of List implementation to use to store multivalues in
     * @param index    Index into the list of values to place the new value
     * @return Previous object stored under this key
     */
    public Object put(Object key, Object value, Class listType, int index) {

        // Get the list of values for the key
        List values = (List)super.get(key);

        // If none found, create a new list
        if (values == null) {

            // Create the new instance type of List
            try {
                values = (List)listType.newInstance();
            } catch (IllegalAccessException eae) {
                eae.printStackTrace();
            } catch (InstantiationException inste) {
                inste.printStackTrace();
            }

        }

        // If the value doesn't already exist
        if (!values.contains(value)) {

            // If index is -1, then append to end
            if (index > -1) {
                values.add(index, value);
            } else {
                values.add(value);
            }
        }

        return super.put(key, values);
    }

    /**
     * Method to copy the contents of the Map into the table
     *
     * @param t Map of data to copy
     */
    public void putAll(Map t) {

        if (t instanceof MultiValueHashMap) {

            // Copy the existing values from the table
            Iterator i = t.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                super.put(e.getKey(), e.getValue());
            }
        } else // We are not an instance of the MultiValueHashMap
            super.putAll(t);
    }
}
