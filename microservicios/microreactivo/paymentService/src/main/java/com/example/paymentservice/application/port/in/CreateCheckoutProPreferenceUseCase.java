package com.example.paymentservice.application.port.in;


import com.acme.payments.application.command.CreateCheckoutProCommand;
import com.acme.payments.application.result.CreateCheckoutProResult;
import reactor.core.publisher.Mono;

public interface CreateCheckoutProPreferenceUseCase {
    Mono<CreateCheckoutProResult> execute(CreateCheckoutProCommand command);
}
