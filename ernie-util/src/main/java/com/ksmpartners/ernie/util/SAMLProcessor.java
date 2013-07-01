package com.ksmpartners.ernie.util;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import com.ksmpartners.ernie.util.MultiValueHashMap;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.common.xml.SAMLSchemaBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

/**
 * Validates a SAML Response and checks the signatures and x509 data against the
 * keystore to see if we trust the sender
 *
 * @author tmulle
 */
public class SAMLProcessor {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(SAMLProcessor.class);

    // Used for the SOAP validation
    private static String WS_SECURITY_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String SECURITY = "Security";
    private static final String CRL_CHECK_PARAM = "saml2.enable.crl.check";

    // Messages
    public static final String READER_CAN_NOT_BE_NULL = "Reader can not be null";
    public static final String COULDNT_CREATE_CLASS = "Couldn't create class";
    public static final String INPUT_STREAM_CAN_NOT_BE_NULL = "InputStream can not be null";
    public static final String ELEMENT_CAN_NOT_BE_NULL = "Element can not be null";
    public static final String CONTENT_CAN_NOT_BE_NULL = "Content can not be null";
    public static final String MISSING_OR_INVALID_WS_SECURITY_HEADER = "Missing or invalid WS-Security Header";
    public static final String MISSING_OR_INVALID_SAML2_ASSERTION_ELEMENT = "Missing or invalid SAML2 Assertion Element";
    public static final String STRING_CAN_NOT_BE_NULL = "String can not be null";
    public static final String MUST_CALL_PARSE_FIRST = "Must call parse() first";
    public static final String KEYSTORE_LOCATION_MUST_BE_SET = "Keystore location must be set";
    public static final String ONLY_SAML_VERSION_2_0_IS_SUPPORTED = "Only SAML version 2.0 is supported";
    public static final String KEYSTORE_LOCATION_CAN_NOT_BE_NULL = "Keystore location can not be null";
    public static final String EMBEDDED_X509_CERTIFICATE_WAS_NOT_FOUND_IN_TRUSTED_KEYSTORE = "Embedded X509 certificate was not found in trusted keystore";
    public static final String KEYSTORE_CERT_FAILED_VERIFICATION = "Certificate in the keystore failed verification: ";
    public static final String SAML_CERT_FAILED_VERIFICATION = "Certificate in the SAML token failed verification: ";
    public static final String PREMATURE_REQUEST = "Current Time is NOT after NotBefore time (Premature Request)";
    public static final String EXPIRED_REQUEST = "Current Time is after NotOnOrAfter time (Request Expired)";
    public static final String SAML_ASSERTION_MUST_BE_SIGNED_WITH_X509_CERTIFICATES = "SAML Assertion must be signed with X509 Certificates";

    // Holds the parsed SAML Response
    private Assertion assertion;

    // Keystore location
    private InputStream keyStoreLocation;

    // The time in seconds in the future within which the NotBefore time of an
    // incoming Assertion is valid. The default is 60 seconds.
    private int timeToLive = 60;

    // Holds a multivalue map of name=values where values is a list of items for
    // a key
    private Map attributes;

    // Holds the keystore
    private KeyStore ks;

    // Should we  check certs against CRL
    private boolean CRLCheckEnabled = true;

    static {
        // initialize the opensaml library once
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new IllegalStateException(COULDNT_CREATE_CLASS, e);
        }

    }

    /**
     * Constructor
     */
    public SAMLProcessor() {
        // Should we  check certs against CRL - Defaults to true if not found
        CRLCheckEnabled = Boolean.valueOf(System.getProperty(CRL_CHECK_PARAM, "true"));
        LOG.info("Certificate Revocation List Checking of X509 Certificates is set to " + CRLCheckEnabled);
    }

    /**
     * Validate a SAML Response Assertion for validity against x509
     *
     * @param samlData
     * @throws SAMLValidationException
     */
    public void parse(Reader samlData) throws SAMLParseException {

        // Null check
        if (samlData == null) throw new SAMLParseException(READER_CAN_NOT_BE_NULL);

        // Load the schema
        Schema schema;
        try {
            schema = SAMLSchemaBuilder.getSAML11Schema();
        } catch (SAXException e) {
            throw new SAMLParseException(e);
        }

        // get parser pool manager
        BasicParserPool parserPoolManager = new BasicParserPool();
        parserPoolManager.setNamespaceAware(true);
        parserPoolManager.setIgnoreElementContentWhitespace(true);
        parserPoolManager.setSchema(schema);

        // parse xml file
        Document document;
        try {
            document = parserPoolManager.parse(samlData);
            Element metadataRoot = document.getDocumentElement();
            parse(metadataRoot);
        } catch (Exception e) {
            throw new SAMLParseException(e);
        }
    }

    /**
     * Validate a SAML Response Assertion for validity against x509
     *
     * @param samlData InputStream to read the SAML response
     * @throws SAMLValidationException
     */
    public void parse(InputStream samlData) throws SAMLParseException {
        if (samlData == null) throw new SAMLParseException(INPUT_STREAM_CAN_NOT_BE_NULL);
        parse(new InputStreamReader(samlData));
    }

    /**
     * Parse the SAML respone from the Element
     *
     * @param samlElement
     * @throws Exception
     */
    public void parse(Element samlElement) throws SAMLParseException {

        // Null check
        if (samlElement == null) throw new SAMLParseException(ELEMENT_CAN_NOT_BE_NULL);

        // Get the unmarshaller for the element
        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(samlElement);

        try {
            // Build the assertion object
            assertion = (Assertion) unmarshaller.unmarshall(samlElement);
        } catch (UnmarshallingException e) {
            throw new SAMLParseException(e);
        }
    }

    /**
     * Parse the SAML respone from a String
     *
     * @param samlContent
     * @throws Exception
     */
    public void parse(String samlContent) throws SAMLParseException {

        // Null check
        if (samlContent == null) throw new SAMLParseException(CONTENT_CAN_NOT_BE_NULL);
        parse(new StringReader(samlContent));
    }

    /**
     * Parse a SAML Assertion for inside a SOAP message
     *
     * @param samlData InputStream to read the SAML response
     * @throws SAMLValidationException
     */
    public void parseFromSOAP(InputStream samlData) throws SAMLParseException {
        parseFromSOAP(new InputSource(samlData));
    }

    /**
     * Parse a SAML Assertion for inside a SOAP message
     *
     * @param samlData InputSource to read the SAML response
     * @throws SAMLValidationException
     */
    public void parseFromSOAP(InputSource samlData) throws SAMLParseException {

        // Null check
        if (samlData == null) throw new SAMLParseException(INPUT_STREAM_CAN_NOT_BE_NULL);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        Document d;
        try {
            d = dbf.newDocumentBuilder().parse(samlData);
        } catch (Exception e) {
            throw new SAMLParseException(e);
        }

        // check for wsse:security element under SOAP Header
        NodeList nodes = d.getElementsByTagNameNS(WS_SECURITY_URI, SECURITY);
        if (nodes == null || nodes.getLength() == 0) {
            throw new SAMLParseException(MISSING_OR_INVALID_WS_SECURITY_HEADER);
        }

        // check for SAML Assertion
        NodeList responses = d.getElementsByTagNameNS(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME);
        if (responses == null || responses.getLength() == 0) {
            throw new SAMLParseException(MISSING_OR_INVALID_SAML2_ASSERTION_ELEMENT);
        }

        // Get the response
        Node respNode = responses.item(0);
        parse((Element) respNode);
    }

    /**
     * Parse a SAML Response Assertion for inside a SOAP message
     *
     * @param samlData String containing soap xml
     * @throws SAMLParseException
     */
    public void parseFromSOAP(String samlData) throws SAMLParseException {

        // Null check
        if (samlData == null) throw new SAMLParseException(STRING_CAN_NOT_BE_NULL);
        parseFromSOAP(new ByteArrayInputStream(samlData.getBytes(Charset.forName("UTF-8"))));
    }

    /**
     * Validate a SAML Response Assertion for validity against x509
     *
     * @throws SAMLValidationException
     */
    public void validate() throws SAMLValidationException {

        // Have to load the document first
        if (assertion == null)
            throw new SAMLValidationException(MUST_CALL_PARSE_FIRST);

        // We need a keystore
        if (keyStoreLocation == null && ks == null)
            throw new SAMLValidationException(KEYSTORE_LOCATION_MUST_BE_SET);

        // We only handle SAML2
        if (!(assertion.getVersion().equals(SAMLVersion.VERSION_20))) {
            throw new SAMLValidationException(ONLY_SAML_VERSION_2_0_IS_SUPPORTED);
        }

        // Check the response times
        checkConditions(assertion);

        // Verify trust on the signature
        if (assertion.isSigned()) {
            checkSignatures(assertion);
        } else {
            throw new SAMLValidationException(SAML_ASSERTION_MUST_BE_SIGNED_WITH_X509_CERTIFICATES);
        }
    }

    /**
     * Set the keystore to use
     */
    public void setKeyStore(KeyStore ks) {
        if (ks == null) throw new IllegalArgumentException(KEYSTORE_LOCATION_CAN_NOT_BE_NULL);
        this.ks = ks;
    }

    /**
     * Set the keystore location
     *
     * @param keystoreLoc File path to keystore
     */
    public void setKeystoreLocation(String keystoreLoc) {
        if (keystoreLoc == null)
            throw new IllegalArgumentException(KEYSTORE_LOCATION_CAN_NOT_BE_NULL);
        try {
            this.keyStoreLocation = new FileInputStream(keystoreLoc);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Set the location of the keystore
     *
     * @param keystoreLoc Inputstream to keystore
     */
    public void setKeystoreLocation(InputStream keystoreLoc) {
        if (keystoreLoc == null)
            throw new IllegalArgumentException(KEYSTORE_LOCATION_CAN_NOT_BE_NULL);
        this.keyStoreLocation = keystoreLoc;
    }

    /**
     * Set the time in seconds in the future within which the NotBefore time of
     * an incoming Assertion is valid. The default is 60 seconds.
     */
    public void setTimeToLive(int seconds) {
        this.timeToLive = seconds;
    }

    /**
     * Method to determine if the passed in value is NULL
     *
     * @param sValue Value to test
     * @return True if value is null, false otherwise
     */
    public static boolean isNull(String sValue) {
        return (sValue == null);
    }

    /**
     * Method to determine if the passed in value is empty ""
     *
     * @param sValue Value to test
     * @return True if value is empty string, false otherwise
     */
    public static boolean isEmpty(String sValue) {

        // If value is null, string is considered empty
        if (isNull(sValue))
            return true;

        // If value is not null, test the length of the string
        int length = sValue.trim().length();

        if (length > 0)
            return false;
        else
            return true;

    }

    /**
     * Method to return if the String is NULL or Empty
     *
     * @param sValue Value to test
     * @return true if it is either null or empty
     */
    public static boolean isNullOrEmpty(String sValue) {
        return (isNull(sValue) || isEmpty(sValue));
    }

    /**
     * Build the list of attributes
     *
     * @param asst Assertion element
     */
    private void buildAttributeList(Assertion asst) {
        Map roles = new MultiValueHashMap();

        List<AttributeStatement> attribs = asst.getAttributeStatements();
        for (AttributeStatement st : attribs) {
            List<Attribute> as = st.getAttributes();
            for (Attribute a : as) {
                List<XMLObject> values = a.getAttributeValues();

                // Get the name
                String name = a.getName();
                if (isNullOrEmpty(name)) name = a.getFriendlyName();

                // Build up the list of values
                for (XMLObject obj : values) {
                    roles.put(name, obj.getDOM().getFirstChild().getTextContent());
                }
            }
        }

        attributes = roles;
    }

    /**
     * Return the attribute map
     *
     * @return
     */
    public Map getAttributes() {

        if (assertion == null)
            throw new IllegalStateException(MUST_CALL_PARSE_FIRST);

        // If not built then build an return
        if (attributes == null) {
            buildAttributeList(assertion);
        }

        return attributes;
    }

    /**
     * Check the signature on the assertion
     *
     * @param asert
     * @throws SAMLValidationException
     */
    private void checkSignatures(Assertion asert) throws SAMLValidationException {

        // Get the signature from the assertion
        Signature sig = asert.getSignature();

        // Validator for the structure of the Signature Section this
        // just
        // checks the XML
        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
        try {
            profileValidator.validate(sig);
            LOG.debug("Signature Structure is valid");
        } catch (ValidationException e) {
            throw new SAMLValidationException(e.getMessage(), e);
        }

        // Get the x509 data so we can check for trust
        List<X509Data> datas = sig.getKeyInfo().getX509Datas();
        for (X509Data data : datas) {

            // Get a list of the certificates
            List<X509Certificate> certs = data.getX509Certificates();
            LOG.debug("Found {} X509 certs to process", new Object[]{certs.size()});

            // Loop through the certs
            for (X509Certificate c : certs) {

                // Convert to real cert
                java.security.cert.X509Certificate sent;
                try {
                    sent = SecurityHelper.buildJavaX509Cert(c.getValue());
                } catch (CertificateException e1) {
                    throw new SAMLValidationException(e1.getMessage(), e1);
                }

                // try to verify certificate from the CRL (Certificate
                // Revocation Lists)
                if (CRLCheckEnabled) {
                    try {
                        LOG.debug("Verifying received certificate against Certificate Revocation List(s)");
                        CRLVerifier.verifyCertificateCRLs(sent);
                    } catch (CertificateVerificationException e1) {
                        throw new SAMLValidationException(SAML_CERT_FAILED_VERIFICATION + e1.getMessage(), e1);
                    }
                }

                // Check if it's in the key store by looking for the
                // sent certificate in our "trusted" keystore
                try {
                    if (ks == null) {
                        ks = KeyStore.getInstance(KeyStore.getDefaultType());
                        ks.load(keyStoreLocation, null);
                    }
                } catch (Exception e2) {
                    throw new SAMLValidationException(e2.getMessage(), e2);
                }

                // Find the matching cert
                java.security.cert.X509Certificate trusted = findMatchingCert(ks, sent);

                // Not found? then not valid
                if (trusted == null) {
                    LOG.warn("Embedded X509 certificate was not found in trusted keystore");
                    throw new SAMLValidationException(EMBEDDED_X509_CERTIFICATE_WAS_NOT_FOUND_IN_TRUSTED_KEYSTORE);
                }

                // try to verify certificate from the CRL (Certificate
                // Revocation Lists)
                if (CRLCheckEnabled) {
                    try {
                        LOG.debug("Verifying keystore certificate against Certificate Revocation List(s)");
                        CRLVerifier.verifyCertificateCRLs(trusted);
                    } catch (CertificateVerificationException e1) {
                        throw new SAMLValidationException(KEYSTORE_CERT_FAILED_VERIFICATION + e1.getMessage(), e1);
                    }
                }

                // Neither were revoked? Check for validity for both
                try {
                    LOG.debug("Checking sent certificate for validity");
                    sent.checkValidity();
                } catch (Exception e) {
                    throw new SAMLValidationException(e.getMessage(), e);
                }

                try {
                    LOG.debug("Checking trusted certificate for validity");
                    trusted.checkValidity();
                } catch (Exception e) {
                    throw new SAMLValidationException(e.getMessage(), e);
                }

                // We are using KeyStore
                BasicX509Credential verificationCredential = new BasicX509Credential();
                verificationCredential.setEntityCertificate(sent);

                // Create a signature validator
                SignatureValidator sigValidator = new SignatureValidator(verificationCredential);
                try {
                    sigValidator.validate(sig);
                } catch (ValidationException e) {
                    throw new SAMLValidationException(e.getMessage(), e);
                }
                LOG.debug("Signature content was matched against keystore");
            }
        }
    }

    /**
     * Check the option conditions NotBefore/NotOnOrAfter
     *
     * @param asert
     * @throws SAMLValidationException
     */
    private void checkConditions(Assertion asert) throws SAMLValidationException {

        // Check the conditions these are optional
        Conditions conditions = asert.getConditions();
        DateTime validFrom = conditions.getNotBefore();
        DateTime validTill = conditions.getNotOnOrAfter();

        // These are optional
        if (validFrom != null) {
            DateTime currentTime = new DateTime();
            currentTime = currentTime.plusSeconds(timeToLive);
            if (validFrom.isAfter(currentTime)) {
                LOG.debug("SAML Token condition (Not Before) not met");
                throw new SAMLValidationException(PREMATURE_REQUEST);
            }
        }

        if (validTill != null && validTill.isBeforeNow()) {
            LOG.debug("SAML Token condition (Not On Or After) not met");
            throw new SAMLValidationException(EXPIRED_REQUEST);
        }
    }

    /**
     * Find the certificate in the keystore that matches the one passed in
     *
     * @param cert The certificate to retrieve from the KeyStore
     * @return The matching certificate or null if not in the KeyStore
     * @throws SAMLValidationException
     */
    private java.security.cert.X509Certificate findMatchingCert(KeyStore ks, java.security.cert.X509Certificate cert)
            throws SAMLValidationException {
        java.security.cert.X509Certificate theCert = null;
        try {
            String alias = ks.getCertificateAlias(cert);
            if (alias != null) {
                // Get the cert
                theCert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
            }
        } catch (KeyStoreException e) {
            throw new SAMLValidationException(e.getMessage(), e);
        }
        return theCert;
    }
}