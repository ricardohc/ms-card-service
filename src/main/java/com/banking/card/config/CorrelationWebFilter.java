package com.banking.card.config;

import com.banking.card.util.AppConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class CorrelationWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String corrHeader = exchange.getRequest().getHeaders().getFirst(AppConstants.HEADER_CORRELATION_ID);
        String channelHeader = exchange.getRequest().getHeaders().getFirst(AppConstants.HEADER_CHANNEL_ID);

        final String correlationId = (corrHeader != null) ? corrHeader : java.util.UUID.randomUUID().toString();
        final String channelId = (channelHeader != null) ? channelHeader : AppConstants.DEFAULT_CHANNEL;

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx
                        .put(AppConstants.HEADER_CORRELATION_ID, correlationId)
                        .put(AppConstants.HEADER_CHANNEL_ID, channelId)
                );
    }
}