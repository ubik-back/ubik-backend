package com.ubik.paymentservice.domain.port.out;

import reactor.core.publisher.Mono;

public interface MotelMpPort {
    record MotelMpCredentials(String accessToken, String publicKey) {}
    Mono<MotelMpCredentials> getCredentials(Long motelId);
}
