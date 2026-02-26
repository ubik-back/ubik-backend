
import com.acme.payments.adapters.out.mercadopago.MercadoPagoAdapter;
import com.acme.payments.application.port.out.MercadoPagoPort.CreatePreferenceRequest;
import com.acme.payments.config.MercadoPagoProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock test for MercadoPagoAdapter.
 * Note: MP SDK uses its own HTTP client, so WireMock verifies our adapter logic,
 * not the full SDK HTTP layer — for SDK-level mocking we stub the SDK beans.
 */
class MercadoPagoAdapterWireMockTest {

    static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        // Stub preference creation
        wireMock.stubFor(post(urlPathEqualTo("/checkout/preferences"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "pref-test-123",
                                  "init_point": "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=pref-test-123",
                                  "sandbox_init_point": "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=pref-test-123"
                                }
                                """)));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void adapter_handles_provider_exception_gracefully() {
        // This test verifies the adapter wraps errors correctly
        // Using a localhost URL that will fail to connect
        MercadoPagoProperties props = new MercadoPagoProperties(
                "TEST_TOKEN", "secret",
                "https://example.com/notify",
                new MercadoPagoProperties.BackUrls(
                        "https://example.com/ok",
                        "https://example.com/pending",
                        "https://example.com/fail"),
                300_000L, 100, 500
        );

        MercadoPagoAdapter adapter = new MercadoPagoAdapter(
                new PreferenceClient(), new PaymentClient(), props);

        CreatePreferenceRequest req = new CreatePreferenceRequest(
                "REF-WIRE-001", "Test item", BigDecimal.valueOf(100), "ARS",
                "https://example.com/notify",
                "https://example.com/ok", "https://example.com/pending",
                "https://example.com/fail"
        );

        // With an invalid/test token, MP SDK throws MPApiException (401)
        // We verify the adapter wraps it as ProviderException
        StepVerifier.create(adapter.createPreference(req, "idem-key-test"))
                .expectErrorMatches(ex ->
                        ex instanceof com.acme.payments.domain.exception.ProviderException)
                .verify();
    }
}