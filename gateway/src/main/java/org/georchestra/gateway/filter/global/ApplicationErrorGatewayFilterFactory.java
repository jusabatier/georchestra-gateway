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

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filter to allow custom error pages to be used when an application behind the
 * gateways returns an error.
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

    private static class ServiceErrorGatewayFilter implements GatewayFilter, Ordered {

        public @Override Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

            ApplicationErrorConveyorHttpResponse response;
            response = new ApplicationErrorConveyorHttpResponse(exchange.getResponse());

            exchange = exchange.mutate().response(response).build();
            return chain.filter(exchange);
        }

        @Override
        public int getOrder() {
            return ResolveTargetGlobalFilter.ORDER + 1;
        }

    }

    /**
     * A response decorator that throws a {@link ResponseStatusException} at
     * {@link #setStatusCode(HttpStatus)} if the status code is an error code, thus
     * letting the gateway render the appropriate custom error page instead of the
     * original application response body.
     */
    private static class ApplicationErrorConveyorHttpResponse extends ServerHttpResponseDecorator {

        public ApplicationErrorConveyorHttpResponse(ServerHttpResponse delegate) {
            super(delegate);
        }

        @Override
        public boolean setStatusCode(@Nullable HttpStatus status) {
            checkStatusCode(status);
            return super.setStatusCode(status);
        }

        private void checkStatusCode(HttpStatus statusCode) {
            log.debug("native status code: {}", statusCode);
            if (statusCode.is4xxClientError() || statusCode.is5xxServerError()) {
                log.debug("Conveying {} response status", statusCode);
                throw new ResponseStatusException(statusCode);
            }
        }
    }
}
