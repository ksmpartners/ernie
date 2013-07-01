package com.ksmpartners.ernie.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base64 encoder and decoder
 *
 * The MIME (Multipurpose Internet Mail Extensions) specification, defined in RFC 2045, lists "base64" as one
 * of several binary to text encoding schemes. MIME's base64 encoding is based on that of the RFC 1421 version
 * of PEM: it uses the same 64-character alphabet and encoding mechanism as PEM, and uses the "=" symbol for
 * output padding in the same way.
 *
 * MIME does not specify a fixed length for base64-encoded lines, but it does specify a maximum length of 76
 * characters. Additionally it specifies that any extra-alphabetic characters must be ignored by a compliant
 * decoder, although most implementations use a CR/LF newline pair to delimit encoded lines.
 *
 * Thus, the actual length of MIME-compliant base64-encoded binary data is usually about 137% of the original
 * data length, though for very short messages the overhead can be a lot higher.
 *
 *@author tmulle
 */
public class Base64Util {

    // $JL-I18N$

    // buffer processing size
    private static final int BUFFER_SIZE = 1024;

    // String character encoding
    public static final String ENCODING = "UTF-8";

    // Chunk size per RFC 2045 section 6.8
    private static final int CHUNK_SIZE = 76;

    // Chunk separator per RFC 2045 section 2.1
    private static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();

    // Code characters for values 0..63
    private static char[] B64CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    // lookup table for converting base64 characters to value in range 0..63
    private static byte[] B64LOOKUP = new byte[256];
    static {
        for (int i = 0; i < 256; i++) B64LOOKUP[i] = -1;
        for (int i = 0; i < B64CHARS.length; i++) {
            B64LOOKUP[B64CHARS[i]] = (byte) i;
        }

        B64LOOKUP['='] = -1;
    }

    /**
     * Base64 encodes the specified string.  The encoded string is returned.
     *
     * @param value    string to be encoded.
     * @return String
     */
    public static String encodeString(String value) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encode(in, out);
        return out.toString();
    }

    /**
     * Base64 encodes the specified string.  The encoded string is returned.
     *
     * @param value    string to be encoded.
     * @return byte[]
     */
    public static byte[] encode(String value) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encode(in, out);
        return out.toByteArray();
    }

    /**
     * Base64 encodes the specified input.  The encoded bytes is returned.
     *
     * @param bytes   bytes to be encoded.
     * @return bytes
     */
    public static byte[] encode(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encode(in, out);
        return out.toByteArray();
    }

    /**
     * Base64 encodes the specified input.  The encoded bytes is returned.
     *
     * @param in   input stream to be encoded.
     * @return bytes
     */
    public static byte[] encode(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encode(in, out);
        return out.toByteArray();
    }

    /**
     * This method runs through the input stream, encoding it to the output
     * stream.
     */
    public static void encode(InputStream in, OutputStream out) throws IOException {
        byte buffer[] = new byte[BUFFER_SIZE];

        int c = -1;
        int pos = 0;
        int bytesWritten = 0;
        int remainder = 0;

        // read up to the buffer size number of characters, and loop until all input has been read
        while ((c = in.read(buffer, remainder, BUFFER_SIZE - remainder)) > 0) {

            // num characters is leftover, plus number chars read
            c += remainder;

            // The first step is to convert three bytes to four numbers of six bits. Each character in the
            // ASCII standard consists of seven bits. Base64 only uses 6 bits (corresponding to 2^6 = 64
            // characters) to ensure encoded data is printable and humanly readable. None of the special
            // characters available in ASCII are used. The 64 characters (hence the name Base64) are 10
            // digits, 26 lowercase characters, 26 uppercase characters as well as '+' and '/'.

            while (pos + 3 <= c) {
                bytesWritten = writeEncodedBytes(out, bytesWritten, buffer, pos, c - pos);
                pos += 3;
            }

            // number of bytes left in buffer...
            remainder = c - pos;

            // copy the remaining bytes to beginning of buffer
            for (int i = 0; i < 3 ; i++) {
                buffer[i] = (i < remainder) ? buffer[pos + i] : ((byte) 0);
            }

            // reset offset
            pos = 0;
        }

        // Write any remaining bytes
        if (remainder > 0) {
            bytesWritten = writeEncodedBytes(out, bytesWritten, buffer, pos, remainder);
        }

        return;
    }

    /**
     * Decodes the specified string.
     *
     * @param value   string to decode
     * @return String
     */
    public static String decodeString(String value) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        decode(in, out);
        return out.toString(ENCODING);
    }

    /**
     * Decodes the specified string.
     *
     * @param value   string to decode
     * @return byte[]
     */
    public static byte[] decode(String value) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        decode(in, out);
        return out.toByteArray();
    }

    /**
     * Decodes the specified bytes.
     *
     * @param bytes   string to decode
     * @return byte[]
     */
    public static byte[] decode(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        decode(in, out);
        return out.toByteArray();
    }

    /**
     * Create a decoder to decode a String to the specified output stream
     *
     * @param value   string to be decoded.
     */
    public static void decode(String value, OutputStream out) throws IOException {
        decode(new ByteArrayInputStream(value.getBytes(ENCODING)), out);
    }

    /**
     * Create a decoder to decode a stream.
     *
     * @param in  The input stream (to be decoded).
     * @param out The output stream, to write decoded data to.
     */
    public static void decode(InputStream in, OutputStream out) throws IOException {
        byte buffer[] = new byte[BUFFER_SIZE];

        int shift = 0; // # of excess bits stored in accum
        int accum = 0; // excess bits
        int index = 0;

        int c = -1;

        // read through the input stream
        while ((c = in.read(buffer)) > 0) {

            for (int i = 0; i < c; i++) {
                int value = B64LOOKUP[buffer[i] & 0xFF]; // ignore high byte of char

                // skip over non-code
                if (value >= 0) {
                    accum <<= 6; // bits shift up by 6 each time thru
                    shift += 6; // loop, with new bits being put in
                    accum |= value; // at the bottom.

                    // whenever there are 8 or more shifted in,
                    if (shift >= 8) {
                        shift -= 8;

                        // write them out from the top, leaving any excess at the bottom for next iteration.
                        out.write((byte) ((accum >> shift) & 0xff));
                    }
                }
            }
        }
    }

    /**
     * Write out the next four bytes.  Will write a chunk separator when the chunk size number
     * of characters has been written.
     *
     * @param out			output stream
     * @param bytesWritten  number of bytes written so far
     * @param bytes         data buffer
     * @param pos           output index
     * @param remainder      number of bytes left to write
     *
     * @return current bytes witten on line
     */
    private static final int writeEncodedBytes(OutputStream out, int bytesWritten, byte[] bytes, int pos, int remainder) throws IOException {
        int index = -1;

        // always write out 4 bytes
        for (int i = 1; i <= 4; i++) {
            switch(i) {
                case 1: {
                    index =  (bytes[pos] & 0xfc) >> 2;
                    break;
                }
                case 2: {
                    index = ((bytes[pos] & 0x3) << 4) | ((bytes[pos + 1] & 0xf0) >>> 4);
                    break;
                }
                case 3: {
                    if (remainder <= 1) {
                        index = 64;
                    } else {
                        index = ((bytes[pos + 1] & 0x0f) << 2) | ((bytes[pos + 2] & 0xc0) >>> 6);
                    }
                    break;
                }
                case 4: {
                    if (remainder <= 2) {
                        index = 64;
                    } else {
                        index = bytes[pos + 2] & 0x3f;
                    }
                    break;
                }
            }

            out.write(B64CHARS[index]);

            if (++bytesWritten == CHUNK_SIZE) {
                out.write(CHUNK_SEPARATOR);
                bytesWritten = 0;
            }
        }
        return bytesWritten;
    }

}
