
import com.acme.payments.adapters.in.web.security.MercadoPagoWebhookSignatureVerifier;
import com.acme.payments.config.MercadoPagoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoWebhookSignatureVerifierTest {

    private MercadoPagoWebhookSignatureVerifier verifier;
    private static final String SECRET = "test_webhook_secret";

    @BeforeEach
    void setUp() {
        MercadoPagoProperties props = new MercadoPagoProperties(
                "access_token", SECRET, "https://example.com/webhook",
                new MercadoPagoProperties.BackUrls(
                        "https://example.com/success",
                        "https://example.com/pending",
                        "https://example.com/failure"),
                300_000L, 2000, 5000
        );
        verifier = new MercadoPagoWebhookSignatureVerifier(props);
    }

    @Test
    void valid_signature_returns_true() throws Exception {
        String dataId = "12345";
        String xRequestId = "req-abc-123";
        long ts = System.currentTimeMillis() / 1000;

        String template = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        String v1 = computeHmac(SECRET, template);

        String xSignature = "ts=" + ts + ",v1=" + v1;

        boolean result = verifier.verify(xSignature, xRequestId, dataId);
        assertThat(result).isTrue();
    }

    @Test
    void invalid_signature_returns_false() {
        String xSignature = "ts=" + (System.currentTimeMillis() / 1000) + ",v1=invalidsignature";
        boolean result = verifier.verify(xSignature, "req-123", "dataId-456");
        assertThat(result).isFalse();
    }

    @Test
    void null_signature_returns_false() {
        assertThat(verifier.verify(null, "req", "data")).isFalse();
    }

    @Test
    void expired_timestamp_returns_false() throws Exception {
        String dataId = "999";
        String xRequestId = "req-old";
        long oldTs = (System.currentTimeMillis() / 1000) - 600; // 10 min ago

        String template = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + oldTs + ";";
        String v1 = computeHmac(SECRET, template);

        String xSignature = "ts=" + oldTs + ",v1=" + v1;
        boolean result = verifier.verify(xSignature, xRequestId, dataId);
        assertThat(result).isFalse();
    }

    private String computeHmac(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
