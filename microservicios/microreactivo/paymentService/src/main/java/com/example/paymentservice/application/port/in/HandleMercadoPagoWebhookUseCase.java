package com.example.paymentservice.application.port.in;


import com.acme.payments.application.command.HandleWebhookCommand;
import reactor.core.publisher.Mono;

public interface HandleMercadoPagoWebhookUseCase {
    Mono<Void> execute(HandleWebhookCommand command);
}
