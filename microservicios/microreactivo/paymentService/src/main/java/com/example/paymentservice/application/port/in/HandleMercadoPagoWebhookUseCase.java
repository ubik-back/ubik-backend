package com.example.paymentservice.application.port.in;


import com.example.paymentservice.application.command.HandleWebhookCommand;
import reactor.core.publisher.Mono;

public interface HandleMercadoPagoWebhookUseCase {
    Mono<Void> execute(HandleWebhookCommand command);
}
