package com.example.paymentservice.application.port.in;


import com.acme.payments.application.command.RefundPaymentCommand;
import com.acme.payments.application.result.RefundResult;
import reactor.core.publisher.Mono;

public interface RefundPaymentUseCase {
    Mono<RefundResult> execute(RefundPaymentCommand command);
}
