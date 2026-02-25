package com.example.paymentservice.application.port.in;


import com.example.paymentservice.application.command.CreateCheckoutProCommand;
import com.example.paymentservice.application.result.CreateCheckoutProResult;
import reactor.core.publisher.Mono;

public interface CreateCheckoutProPreferenceUseCase {
    Mono<CreateCheckoutProResult> execute(CreateCheckoutProCommand command);
}
