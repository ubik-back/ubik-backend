package com.example.paymentservice.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(dialect, List.of(
                new LocalDateTimeToInstantConverter(),
                new InstantToLocalDateTimeConverter()
        ));
    }

    @ReadingConverter
    static class LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {
        @Override
        public Instant convert(LocalDateTime source) {
            return source.toInstant(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    static class InstantToLocalDateTimeConverter implements Converter<Instant, LocalDateTime> {
        @Override
        public LocalDateTime convert(Instant source) {
            return LocalDateTime.ofInstant(source, ZoneOffset.UTC);
        }
    }
}
