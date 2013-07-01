package com.ksmpartners.ernie.util;

import com.ksmpartners.ernie.util.*;
import org.opensaml.xml.validation.ValidationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

/**
 * SAML processor test
 *
 * This tests the basics. We can't test full validation because
 * modifying the xml files will cause the validation to fail due to the encryption
 */
public class SAMLProcessorTest {

    private InputStream in;
    private InputStream badVersion;
    private InputStream keystore;
    private InputStream soapIn;
    private SAMLProcessor saml;

    @BeforeMethod
    public void setup() {

        // Inputs
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/SAML/openam.xml");
        soapIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/SAML/ScheduleSOAPReq.xml");
        badVersion = Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/SAML/openam_bad_version.xml");
        keystore = Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/SAML/keystore.jks");

        // Create the processor
        saml = new SAMLProcessor();
        Assert.assertNotNull(saml);
    }

    /**
     * Test SOAP parsing
     */
    @Test
    public void testSOAPParse() {

        // Soap good
        try {
            saml.parseFromSOAP(soapIn);
        } catch (SAMLParseException e) {
            Assert.fail("Something went wrong with parsing", e);
        }

        // Non SOAP message
        try {
            saml.parseFromSOAP(in);
            Assert.fail("Should've gotten an exception");
        } catch (SAMLParseException e) {
            Assert.assertTrue(e.getMessage().contains(SAMLProcessor.MISSING_OR_INVALID_WS_SECURITY_HEADER));
        }
    }

    /**
     * Test SOAP validate
     */
    @Test
    public void testSOAPValidate() {

    }

    /**
     * Test reading attributes
     */
    @Test
    public void testAttributes() {

        // Should get exception
        try {
            saml.getAttributes();
            Assert.fail("Should've gotten an exception");
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.MUST_CALL_PARSE_FIRST), message);
        }
    }

    /**
     * Test null input parsing
     */
    @Test
    public void testNullInputParsing() {

        // Null input parsing
        try {
            saml.parse((InputStream) null);
            Assert.fail("Should've gotten an exception");
        } catch (SAMLParseException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.INPUT_STREAM_CAN_NOT_BE_NULL), message);
        }

        // Null input parsing
        try {
            saml.parse((Reader)null);
            Assert.fail("Should've gotten an exception");
        } catch (SAMLParseException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.READER_CAN_NOT_BE_NULL), message);
        }

        // Null input parsing
        try {
            saml.parse((Element)null);
            Assert.fail("Should've gotten an exception");
        } catch (SAMLParseException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.ELEMENT_CAN_NOT_BE_NULL), message);
        }
    }

    /**
     * Test keystore inputs
     */
    @Test
    public void testKeystoreLocation() {

        // Null keystore location
        try {
            saml.setKeystoreLocation((String)null);
            Assert.fail("Should've gotten an exception");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.KEYSTORE_LOCATION_CAN_NOT_BE_NULL), message);
        }

        // Null keystore location
        try {
            saml.setKeystoreLocation((InputStream)null);
            Assert.fail("Should've gotten an exception");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.KEYSTORE_LOCATION_CAN_NOT_BE_NULL), message);
        }

        // Null keystore location
        try {
            saml.setKeystoreLocation("This is bad");
            Assert.fail("Should've gotten an exception");
        } catch (IllegalArgumentException e) {
            Throwable cause = e.getCause();
            Assert.assertTrue(cause instanceof FileNotFoundException);
        }
    }

    /**
     * Test validating
     */
    @Test
    public void testValidating() {

        // Should get exception
        try {
            saml.validate();
            Assert.fail("Should've gotten an exception");
        } catch (SAMLValidationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.MUST_CALL_PARSE_FIRST), message);
        }

        // Null keystore before validating
        try {
            saml.parse(in);
            saml.validate();
            Assert.fail("Should've gotten an exception");
        } catch (SAMLValidationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.KEYSTORE_LOCATION_MUST_BE_SET),message);
        } catch (SAMLParseException e) {
           Assert.fail("Something went wrong with parsing", e);
        }

        // Non supported version
        try {
            saml.setKeystoreLocation(keystore);
            saml.parse(badVersion);
            saml.validate();
            Assert.fail("Should've gotten an exception");
        } catch (SAMLValidationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains(SAMLProcessor.ONLY_SAML_VERSION_2_0_IS_SUPPORTED), message);
        } catch (SAMLParseException e) {
            Assert.fail("Something went wrong with parsing", e);
        }
    }
}
