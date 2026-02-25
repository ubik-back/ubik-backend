package com.example.paymentservice.application.port.in;


import com.example.paymentservice.application.command.RefundPaymentCommand;
import com.example.paymentservice.application.result.RefundResult;
import reactor.core.publisher.Mono;

public interface RefundPaymentUseCase {
    Mono<RefundResult> execute(RefundPaymentCommand command);
}
