package com.example.paymentservice.adapters.in.web;


import com.example.paymentservice.adapters.in.web.dto.PaymentDtos;
import com.example.paymentservice.adapters.in.web.dto.PaymentDtos.*;
import com.example.paymentservice.application.command.CreateCheckoutProCommand;
import com.example.paymentservice.application.command.RefundPaymentCommand;
import com.example.paymentservice.application.port.in.CreateCheckoutProPreferenceUseCase;
import com.example.paymentservice.application.port.in.GetPaymentIntentUseCase;
import com.example.paymentservice.application.port.in.RefundPaymentUseCase;
import com.example.paymentservice.application.result.PaymentIntentView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentIntentController {

    private final CreateCheckoutProPreferenceUseCase createUseCase;
    private final GetPaymentIntentUseCase getUseCase;
    private final RefundPaymentUseCase refundUseCase;

    public PaymentIntentController(CreateCheckoutProPreferenceUseCase createUseCase,
                                   GetPaymentIntentUseCase getUseCase,
                                   RefundPaymentUseCase refundUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.refundUseCase = refundUseCase;
    }

    @PostMapping("/checkout-pro")
    public Mono<ResponseEntity<CreateCheckoutProResponse>> createCheckoutPro(
            @Valid @RequestBody CreateCheckoutProRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        var command = new CreateCheckoutProCommand(
                req.externalReference(),
                req.title(),
                req.amount(),
                req.currency(),
                idempotencyKey,
                userId
        );

        return createUseCase.execute(command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new CreateCheckoutProResponse(
                                result.paymentIntentId().toString(),
                                result.externalReference(),
                                result.initPoint(),
                                result.status()
                        )));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PaymentIntentResponse>> getPaymentIntent(
            @PathVariable UUID id) {
        return getUseCase.execute(id)
                .map(view -> ResponseEntity.ok(toResponse(view)));
    }

    @PostMapping("/{id}/refunds")
    public Mono<ResponseEntity<RefundResponse>> refund(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        var command = new RefundPaymentCommand(id, req.amount(), idempotencyKey);

        return refundUseCase.execute(command)
                .map(result -> ResponseEntity.ok(new RefundResponse(
                        result.paymentIntentId().toString(),
                        result.refundId(),
                        result.refundedAmount(),
                        result.status()
                )));
    }

    private PaymentIntentResponse toResponse(PaymentIntentView view) {
        return new PaymentIntentResponse(
                view.id().toString(),
                view.externalReference(),
                view.amount(),
                view.currency(),
                view.status().name(),
                view.provider(),
                view.mpPreferenceId(),
                view.mpPaymentId(),
                view.redirectUrl(),
                view.lastProviderStatus(),
                view.createdAt() != null ? view.createdAt().toString() : null,
                view.updatedAt() != null ? view.updatedAt().toString() : null
        );
    }
}