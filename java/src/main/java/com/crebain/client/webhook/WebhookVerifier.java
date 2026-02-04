package com.crebain.client.webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Webhook signature verification utilities.
 * <p>
 * Use these to verify incoming webhook requests from the Crebain API.
 *
 * <pre>{@code
 * // In your webhook handler (e.g., Spring Controller)
 * @PostMapping("/webhook")
 * public ResponseEntity<String> handleWebhook(
 *         @RequestHeader("X-Crebain-Timestamp") String timestamp,
 *         @RequestHeader("X-Crebain-Signature") String signature,
 *         @RequestBody byte[] rawBody) {
 *
 *     if (!WebhookVerifier.verify(WEBHOOK_SECRET, timestamp, rawBody, signature)) {
 *         return ResponseEntity.status(401).body("Invalid signature");
 *     }
 *
 *     // Process the webhook...
 *     return ResponseEntity.ok("OK");
 * }
 * }</pre>
 */
public final class WebhookVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "v1=";

    private WebhookVerifier() {
        // Utility class
    }

    /**
     * Verify a webhook signature.
     * <p>
     * The signature is computed as HMAC-SHA256 of "{timestamp}.{raw_body}"
     * using your webhook secret as the key.
     *
     * @param secret          Your webhook secret (the one you provided when creating the subscription)
     * @param timestamp       Value of the X-Crebain-Timestamp header
     * @param rawBody         Raw request body as bytes (do NOT parse or modify)
     * @param signatureHeader Value of the X-Crebain-Signature header (format: "v1=&lt;hex&gt;")
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verify(String secret, String timestamp, byte[] rawBody, String signatureHeader) {
        if (secret == null || timestamp == null || rawBody == null || signatureHeader == null) {
            return false;
        }

        if (!signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            return false;
        }

        String expectedSig = signatureHeader.substring(SIGNATURE_PREFIX.length());
        String computedSig = computeSignatureHex(secret, timestamp, rawBody);

        // Timing-safe comparison
        return MessageDigest.isEqual(
                expectedSig.getBytes(StandardCharsets.UTF_8),
                computedSig.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Compute a webhook signature (useful for testing).
     *
     * @param secret    Webhook secret
     * @param timestamp Unix timestamp as string
     * @param rawBody   Raw request body as bytes
     * @return Signature in "v1=&lt;hex&gt;" format
     */
    public static String computeSignature(String secret, String timestamp, byte[] rawBody) {
        return SIGNATURE_PREFIX + computeSignatureHex(secret, timestamp, rawBody);
    }

    private static String computeSignatureHex(String secret, String timestamp, byte[] rawBody) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);

            // Construct the canonical payload: timestamp.body
            byte[] timestampBytes = (timestamp + ".").getBytes(StandardCharsets.UTF_8);
            mac.update(timestampBytes);
            mac.update(rawBody);

            byte[] hmacBytes = mac.doFinal();
            return HexFormat.of().formatHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
