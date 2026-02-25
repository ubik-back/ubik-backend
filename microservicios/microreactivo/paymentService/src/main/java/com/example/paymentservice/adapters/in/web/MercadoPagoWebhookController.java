package com.example.paymentservice.adapters.in.web;

import com.example.paymentservice.application.command.HandleWebhookCommand;
import com.example.paymentservice.application.port.in.HandleMercadoPagoWebhookUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments/webhooks")
public class MercadoPagoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    private final HandleMercadoPagoWebhookUseCase webhookUseCase;

    public MercadoPagoWebhookController(HandleMercadoPagoWebhookUseCase webhookUseCase) {
        this.webhookUseCase = webhookUseCase;
    }

    /**
     * Webhook endpoint for Mercado Pago notifications.
     * ALWAYS returns 200 — MP will retry if it receives other status codes.
     */
    @PostMapping("/mercadopago")
    public Mono<ResponseEntity<Void>> handleWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody(required = false) String rawBody) {

        log.info("Webhook received: type={}, dataId={}, xRequestId={}", type, dataId, xRequestId);

        if (type == null || dataId == null) {
            log.warn("Webhook missing type or data.id params");
            return Mono.just(ResponseEntity.ok().build());
        }

        var command = new HandleWebhookCommand(type, dataId, xRequestId, xSignature, rawBody);

        return webhookUseCase.execute(command)
                .thenReturn(ResponseEntity.<Void>ok().build())
                .onErrorResume(ex -> {
                    log.error("Unhandled error in webhook processing: {}", ex.getMessage(), ex);
                    // CRITICAL: always return 200 so MP doesn't retry
                    return Mono.just(ResponseEntity.ok().build());
                });
    }
}