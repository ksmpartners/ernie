/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ksmpartners.ernie.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestTest {

    @Test
    public void testTestClass()
        throws Exception
    {
        TestClass obj = new TestClass("test_name", 1);

        String json = TestUtil.serialize(obj);
        TestClass newObj = TestUtil.deserialize(json, TestClass.class);

        Assert.assertEquals(newObj.getName(), "test_name");

        Assert.assertEquals(json, "{\"name\":\"test_name\",\"id\":1}");
    }

    @Test
    public void testLists()
            throws Exception
    {
        TestClass obj1 = new TestClass("test_name1", 1);
        TestClass obj2 = new TestClass("test_name2", 2);

        List<TestClass> objList = new ArrayList<TestClass>();

        objList.add(obj1);
        objList.add(obj2);

        String json = TestUtil.serialize(objList);
        List<TestClass> newList = TestUtil.MAPPER.readValue(json, new TypeReference<List<TestClass>>(){});

        Assert.assertEquals(json, "[{\"name\":\"test_name1\",\"id\":1},{\"name\":\"test_name2\",\"id\":2}]");
        Assert.assertTrue(TestUtil.equal(objList, newList));
    }

    @Test
    public void testVerify()
        throws Exception
    {
        TestUtil.verifySerialization(TestClass.class);
        TestUtil.verifySerialization(ReportRequest.class);
        TestUtil.verifySerialization(ReportResponse.class);
        TestUtil.verifySerialization(StatusResponse.class);
        TestUtil.verifySerialization(ReportDefinitionMapResponse.class);
        TestUtil.verifySerialization(JobsMapResponse.class);
    }

    @Test
    public void testEqual()
        throws Exception
    {
        Assert.assertFalse(TestUtil.equal(new TestClass("name", 1), new TestClass("name1", 2)));
        Assert.assertTrue(TestUtil.equal(new TestClass("name", 1), new TestClass("name", 1)));
        Assert.assertTrue(TestUtil.equal(TestClass.class.newInstance(), TestClass.class.newInstance()));
        Assert.assertFalse(TestUtil.equal("test string", Integer.valueOf(1)));
    }

}
