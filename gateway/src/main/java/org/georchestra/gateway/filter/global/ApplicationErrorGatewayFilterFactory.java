/*
 * Copyright (C) 2024 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.filter.global;

import java.util.function.Supplier;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filter to allow custom error pages to be used when an application behind the
 * gateways returns an error, only for idempotent HTTP response status codes
 * (i.e. GET, HEAD, OPTIONS).
 * <p>
 * {@link GatewayFilterFactory} providing a {@link GatewayFilter} that throws a
 * {@link ResponseStatusException} with the proxied response status code if the
 * target responded with a {@code 400...} or {@code 500...} status code.
 * 
 * <p>
 * Usage: to enable it globally, add this to application.yaml :
 * 
 * <pre>
 * <code>
 * spring:
 *  cloud:
 *    gateway:
 *      default-filters:
 *        - ApplicationError
 * </code>
 * </pre>
 * 
 * To enable it only on some routes, add this to concerned routes in
 * {@literal routes.yaml}:
 * 
 * <pre>
 * <code>
 *        filters:
 *       - name: ApplicationError
 * </code>
 * </pre>
 */
@Slf4j
public class ApplicationErrorGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    public ApplicationErrorGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(final Object config) {
        return new ServiceErrorGatewayFilter();
    }

    private class ServiceErrorGatewayFilter implements GatewayFilter, Ordered {
        /**
         * @return {@link Ordered#HIGHEST_PRECEDENCE} or
         *         {@link ApplicationErrorConveyorHttpResponse#beforeCommit(Supplier)}
         *         won't be called
         */
        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

        /**
         * If the request method is idempotent and accepts {@literal text/html}, applies
         * a filter that when the routed response receives an error status code, will
         * throw a {@link ResponseStatusException} with the same status, for the gateway
         * to apply the customized error template, also when the status code comes from
         * a proxied service response
         */
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (canFilter(exchange.getRequest())) {
                exchange = decorate(exchange);
            }
            return chain.filter(exchange);
        }
    }

    ServerWebExchange decorate(ServerWebExchange exchange) {
        var response = new ApplicationErrorConveyorHttpResponse(exchange.getResponse());
        exchange = exchange.mutate().response(response).build();
        return exchange;
    }

    boolean canFilter(ServerHttpRequest request) {
        return methodIsIdempotent(request.getMethod()) && acceptsHtml(request);
    }

    boolean methodIsIdempotent(HttpMethod method) {
        return switch (method) {
        case GET, HEAD, OPTIONS, TRACE -> true;
        default -> false;
        };
    }

    boolean acceptsHtml(ServerHttpRequest request) {
        return request.getHeaders().getAccept().stream().anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
    }

    /**
     * A response decorator that throws a {@link ResponseStatusException} at
     * {@link #beforeCommit} if the status code is an error code, thus letting the
     * gateway render the appropriate custom error page instead of the original
     * application response body.
     */
    private static class ApplicationErrorConveyorHttpResponse extends ServerHttpResponseDecorator {

        public ApplicationErrorConveyorHttpResponse(ServerHttpResponse delegate) {
            super(delegate);
        }

        @Override
        public void beforeCommit(Supplier<? extends Mono<Void>> action) {
            Mono<Void> checkStatus = Mono.fromRunnable(this::checkStatusCode);
            Mono<Void> checkedAction = checkStatus.then(Mono.fromRunnable(action::get));
            super.beforeCommit(() -> checkedAction);
        }

        private void checkStatusCode() {
            HttpStatus statusCode = getStatusCode();
            log.debug("native status code: {}", statusCode);
            if (statusCode.is4xxClientError() || statusCode.is5xxServerError()) {
                log.debug("Conveying {} response status", statusCode);
                throw new ResponseStatusException(statusCode);
            }
        }
    }
}
