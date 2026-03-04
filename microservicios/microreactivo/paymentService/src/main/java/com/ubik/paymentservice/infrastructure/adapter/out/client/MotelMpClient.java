package com.ubik.paymentservice.infrastructure.adapter.out.client;

import com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository.MotelMpAccountRepository;
import com.ubik.paymentservice.domain.port.out.MotelMpPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class MotelMpClient implements MotelMpPort {

    private static final Logger log = LoggerFactory.getLogger(MotelMpClient.class);
    private final MotelMpAccountRepository mpAccountRepository;

    public MotelMpClient(MotelMpAccountRepository mpAccountRepository) {
        this.mpAccountRepository = mpAccountRepository;
    }

    @Override
    public Mono<MotelMpCredentials> getCredentials(Long motelId) {
        log.info("Buscando credenciales de MercadoPago para motelId={}", motelId);
        
        return mpAccountRepository.findByMotelId(motelId)
                .map(account -> {
                    log.info("✅ Credenciales encontradas para motel={}", motelId);
                    return new MotelMpCredentials(account.accessToken(), account.publicKey());
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, 
                        "El motel " + motelId + " no ha vinculado su cuenta de MercadoPago en el sistema de pagos (Local DB)."
                )));
    }
}
