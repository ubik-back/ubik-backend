

import com.acme.payments.application.port.in.CreateCheckoutProPreferenceUseCase;
import com.acme.payments.application.port.in.GetPaymentIntentUseCase;
import com.acme.payments.application.port.in.RefundPaymentUseCase;
import com.acme.payments.application.result.CreateCheckoutProResult;
import com.acme.payments.application.result.PaymentIntentView;
import com.acme.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acme.payments.adapters.in.web.PaymentIntentController;
import com.acme.payments.adapters.in.web.exception.GlobalExceptionHandler;
import com.acme.payments.config.SecurityConfig;

@WebFluxTest(controllers = PaymentIntentController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PaymentIntentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CreateCheckoutProPreferenceUseCase createUseCase;

    @MockBean
    private GetPaymentIntentUseCase getUseCase;

    @MockBean
    private RefundPaymentUseCase refundUseCase;

    @Test
    void createCheckoutPro_returns_201_with_initPoint() {
        UUID intentId = UUID.randomUUID();
        when(createUseCase.execute(any()))
                .thenReturn(Mono.just(new CreateCheckoutProResult(
                        intentId, "REF-001",
                        "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123",
                        "PENDING"
                )));

        webTestClient.post().uri("/api/payments/checkout-pro")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "externalReference": "REF-001",
                          "title": "Habitación Doble",
                          "amount": 150.00,
                          "currency": "ARS"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.paymentIntentId").isEqualTo(intentId.toString())
                .jsonPath("$.initPoint").exists();
    }

    @Test
    void getPaymentIntent_returns_200() {
        UUID intentId = UUID.randomUUID();
        when(getUseCase.execute(intentId))
                .thenReturn(Mono.just(new PaymentIntentView(
                        intentId, "REF-001", BigDecimal.valueOf(150), "ARS",
                        PaymentStatus.PENDING, "MERCADO_PAGO",
                        "pref-123", null, "https://mp.com/redirect", null,
                        Instant.now(), Instant.now()
                )));

        webTestClient.get().uri("/api/payments/{id}", intentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(intentId.toString())
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void createCheckoutPro_missing_idempotency_key_returns_400() {
        webTestClient.post().uri("/api/payments/checkout-pro")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "externalReference": "REF-001",
                          "title": "Test",
                          "amount": 100.00,
                          "currency": "ARS"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }
}