package com.crebain.client;

import com.crebain.client.webhook.WebhookVerifier;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WebhookVerifier.
 */
class WebhookVerifierTest {

    private static final String TEST_SECRET = "whsec_test_secret_key";
    private static final String TEST_TIMESTAMP = "1704067200";
    private static final String TEST_BODY = "{\"event\":\"request.complete\",\"request_id\":\"123\"}";

    @Test
    void testComputeSignature() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        assertNotNull(signature);
        assertTrue(signature.startsWith("v1="));
        assertEquals(67, signature.length()); // "v1=" (3 chars) + 64 hex chars
    }

    @Test
    void testVerifyValidSignature() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        boolean valid = WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, body, signature);

        assertTrue(valid);
    }

    @Test
    void testVerifyInvalidSignature() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);

        boolean valid = WebhookVerifier.verify(
                TEST_SECRET,
                TEST_TIMESTAMP,
                body,
                "v1=0000000000000000000000000000000000000000000000000000000000000000"
        );

        assertFalse(valid);
    }

    @Test
    void testVerifyWrongSecret() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        boolean valid = WebhookVerifier.verify("wrong_secret", TEST_TIMESTAMP, body, signature);

        assertFalse(valid);
    }

    @Test
    void testVerifyWrongTimestamp() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        boolean valid = WebhookVerifier.verify(TEST_SECRET, "9999999999", body, signature);

        assertFalse(valid);
    }

    @Test
    void testVerifyWrongBody() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        byte[] differentBody = "{\"different\":\"body\"}".getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        boolean valid = WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, differentBody, signature);

        assertFalse(valid);
    }

    @Test
    void testVerifyMissingV1Prefix() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);

        // Signature without "v1=" prefix should fail
        boolean valid = WebhookVerifier.verify(
                TEST_SECRET,
                TEST_TIMESTAMP,
                body,
                "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
        );

        assertFalse(valid);
    }

    @Test
    void testVerifyNullInputs() {
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        assertFalse(WebhookVerifier.verify(null, TEST_TIMESTAMP, body, signature));
        assertFalse(WebhookVerifier.verify(TEST_SECRET, null, body, signature));
        assertFalse(WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, null, signature));
        assertFalse(WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, body, null));
    }

    @Test
    void testSignatureConsistency() {
        // Verify that the same inputs always produce the same signature
        byte[] body = TEST_BODY.getBytes(StandardCharsets.UTF_8);

        String sig1 = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);
        String sig2 = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        assertEquals(sig1, sig2);
    }

    @Test
    void testEmptyBody() {
        byte[] emptyBody = new byte[0];
        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, emptyBody);

        assertTrue(WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, emptyBody, signature));
    }

    @Test
    void testUnicodeBody() {
        String unicodeContent = "{\"message\":\"Hello \u4e16\u754c\"}";
        byte[] body = unicodeContent.getBytes(StandardCharsets.UTF_8);

        String signature = WebhookVerifier.computeSignature(TEST_SECRET, TEST_TIMESTAMP, body);

        assertTrue(WebhookVerifier.verify(TEST_SECRET, TEST_TIMESTAMP, body, signature));
    }
}
