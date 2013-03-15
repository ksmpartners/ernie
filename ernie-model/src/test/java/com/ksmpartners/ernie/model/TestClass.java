/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

/**
 * Class used for testing serialization and deserialization
 */
public class TestClass {

    private String name;
    private int id;

    // Must have public no-arg constructors
    public TestClass(){}

    public TestClass(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    // Field names in JSON are taken from getter names, unless annotated with @JsonProperty("Alternate Name")
    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return "id: " + id + ", name: " + name;
    }

}
