package com.ksmpartners.ernie.util;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test class for Base64 Encode/Decode
 *
 * @author tmulle
 * @version $Revision: #1 $
 */
public class Base64Test {

    private static final String BASE64_STRING = "SGVsbG8gV29ybGQ=";
    private static final String SECRET_STRING = "Hello World";


    @Test
    public void decode() {

        try {
            assertEquals(SECRET_STRING, Base64Util.decodeString(BASE64_STRING));
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void encode() {
        try {
            assertEquals(BASE64_STRING, Base64Util.encodeString(SECRET_STRING));
        } catch (Exception e) {
            fail();
        }
    }
}
