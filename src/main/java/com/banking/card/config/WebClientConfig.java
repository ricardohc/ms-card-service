package com.banking.card.config;

import com.banking.card.util.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {
                    return Mono.deferContextual(contextView -> {
                        String correlationId = contextView.getOrDefault(AppConstants.HEADER_CORRELATION_ID, UUID.randomUUID().toString());
                        String channelId = contextView.getOrDefault(AppConstants.HEADER_CHANNEL_ID, AppConstants.DEFAULT_CHANNEL);

                        ClientRequest newRequest = ClientRequest.from(request)
                                .header(AppConstants.HEADER_CORRELATION_ID, correlationId)
                                .header(AppConstants.HEADER_CHANNEL_ID, channelId)
                                .build();

                        return next.exchange(newRequest);
                    });
                });
    }
}