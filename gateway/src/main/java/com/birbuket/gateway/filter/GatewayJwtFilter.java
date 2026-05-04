package com.birbuket.gateway.filter;

import com.birbuket.gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayJwtFilter implements WebFilter {

    private final JwtService jwtService;
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth",
            "/api/auth/",
            "/api/cart",
            "/api/cart/",
            "/api/order",
            "/api/order/",
            "/api/plantdoctor",
            "/api/plantdoctor/",
            "/api/product",
            "/api/product/",
            "/api/category",
            "/api/category/",
            "/api/variant",
            "/api/variant/",
            "/uploads",
            "/uploads/",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/swagger-ui/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return "/swagger-ui.html".equals(path);
    }
}