package com.example.paymentservice.adapters.in.web.security;

import com.acme.payments.config.MercadoPagoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

/**
 * Validates Mercado Pago webhook HMAC-SHA256 signatures.
 * Format: "ts=1234567890,v1=abc123..."
 * Template: "id:{dataId};request-id:{xRequestId};ts:{ts};"
 */
@Component
public class MercadoPagoWebhookSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookSignatureVerifier.class);
    private static final String ALGORITHM = "HmacSHA256";

    private final MercadoPagoProperties props;

    public MercadoPagoWebhookSignatureVerifier(MercadoPagoProperties props) {
        this.props = props;
    }

    /**
     * @param xSignature  The x-signature header value "ts=...,v1=..."
     * @param xRequestId  The x-request-id header value
     * @param dataId      The data.id query parameter
     * @return true if valid
     */
    public boolean verify(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xRequestId == null || dataId == null) {
            log.warn("Missing signature headers");
            return false;
        }

        // Parse ts and v1 from header
        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("ts=")) ts = trimmed.substring(3);
            else if (trimmed.startsWith("v1=")) v1 = trimmed.substring(3);
        }

        if (ts == null || v1 == null) {
            log.warn("Could not parse ts or v1 from x-signature header");
            return false;
        }

        // Anti-replay check
        try {
            long tsMillis = Long.parseLong(ts) * 1000L; // ts is in seconds
            long skew = Math.abs(Instant.now().toEpochMilli() - tsMillis);
            if (skew > props.webhookSkewMillis()) {
                log.warn("Webhook timestamp skew too large: {}ms", skew);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid ts value: {}", ts);
            return false;
        }

        // Build template
        String template = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

        // Compute HMAC-SHA256
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    props.webhookSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);
            byte[] computed = mac.doFinal(template.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHex(computed);

            // Constant-time comparison
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error computing webhook signature", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
