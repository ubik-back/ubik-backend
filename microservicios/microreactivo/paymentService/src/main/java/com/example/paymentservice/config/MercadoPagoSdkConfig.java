package com.example.paymentservice.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoSdkConfig {

    @Bean
    public PreferenceClient preferenceClient(MercadoPagoProperties props) {
        MercadoPagoConfig.setAccessToken(props.accessToken());
        return new PreferenceClient();
    }

    @Bean
    public PaymentClient paymentClient(MercadoPagoProperties props) {
        MercadoPagoConfig.setAccessToken(props.accessToken());
        return new PaymentClient();
    }
}
