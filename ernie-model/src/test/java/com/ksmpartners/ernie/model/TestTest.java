package com.ksmpartners.ernie.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTest {

    @Test
    public void testMethod()
        throws Exception
    {
        ObjectMapper om = new ObjectMapper();

        TestClass obj = new TestClass();

        obj.setName("test_name");
        obj.setId(1);

        String json = om.writeValueAsString(obj);

        Assert.assertEquals(json, "{\"name\":\"test_name\",\"id\":1}");
    }

    private class TestClass {

        private String name;
        private int id;

        @JsonProperty("name")
        public String getName()
        {
            return name;
        }

        @JsonProperty
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

    }

}
