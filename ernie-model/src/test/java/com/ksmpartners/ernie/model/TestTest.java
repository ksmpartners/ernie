package com.ksmpartners.ernie.model;

import com.ksmpartners.ernie.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTest {

    @Test
    public void testMethod()
        throws Exception
    {
        TestClass obj = new TestClass("test_name", 1);

        String json = TestUtil.serialize(obj);
        TestClass newObj = TestUtil.deserialize(json, TestClass.class);

        Assert.assertEquals(newObj.getName(), "test_name");

        Assert.assertEquals(json, "{\"name\":\"test_name\",\"id\":1}");
    }

    @Test
    public void testVerify()
        throws Exception
    {
        TestUtil.verifySerialization(TestClass.class);
    }

}
