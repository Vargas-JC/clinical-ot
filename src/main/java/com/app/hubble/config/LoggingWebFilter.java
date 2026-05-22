package com.app.hubble.config;

import com.app.hubble.exception.HandleGlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().toString()
                : "unknown";

        log.info("INBOUND [{}] {} from {}", method, path, ip);

        return chain.filter(exchange)
                .doOnSuccess(ignored -> logOutbound(method, path, exchange, startTime, null))
                .doOnError(throwable -> logOutbound(method, path, exchange, startTime, throwable));
    }

    private void logOutbound(String method, String path, ServerWebExchange exchange, long startTime, Throwable error) {
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        int statusCode = status != null ? status.value() : 200;
        long duration = System.currentTimeMillis() - startTime;
        Object apiMessage = exchange.getAttribute(HandleGlobalException.ERROR_MESSAGE_ATTRIBUTE);
        if (error != null) {
            log.error("OUTBOUND [{}] {} - Status: {} - Error: {} - Time: {}ms",
                    method, path, statusCode, error.getMessage(), duration, error);
            return;
        }
        if (statusCode >= 400) {
            log.warn("OUTBOUND [{}] {} - Status: {} - Body: {} - Time: {}ms",
                    method, path, statusCode, apiMessage != null ? apiMessage : "-", duration);
            return;
        }
        log.info("OUTBOUND [{}] {} - Status: {} - Time: {}ms", method, path, statusCode, duration);
    }
}
