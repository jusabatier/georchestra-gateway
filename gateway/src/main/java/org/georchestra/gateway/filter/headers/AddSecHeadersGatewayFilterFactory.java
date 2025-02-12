/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.filter.headers;

import java.util.Arrays;
import java.util.List;

import org.georchestra.gateway.filter.global.ResolveTargetGlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link AbstractGatewayFilterFactory} that adds geOrchestra-specific security
 * headers to proxied requests.
 * <p>
 * This filter allows customizable security headers to be appended to requests,
 * using a set of {@link HeaderContributor} providers. If the request exchange
 * contains the attribute {@link #DISABLE_SECURITY_HEADERS}, the filter is
 * bypassed.
 * </p>
 * <p>
 * Sample usage in {@code application.yaml} to apply the filter globally:
 * </p>
 * 
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *     gateway:
 *       default-filters:
 *         - AddSecHeaders
 * </code>
 * </pre>
 */
public class AddSecHeadersGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    /**
     * Attribute key to disable the security headers for a specific request. If this
     * attribute is present in the request exchange, the filter is skipped.
     */
    public static final String DISABLE_SECURITY_HEADERS = "%s.DISABLE_SECURITY_HEADERS"
            .formatted(AddSecHeadersGatewayFilterFactory.class.getName());

    private final List<HeaderContributor> providers;

    /**
     * Creates a new instance of the security headers filter factory.
     *
     * @param providers the list of {@link HeaderContributor} providers that
     *                  generate the security headers
     */
    public AddSecHeadersGatewayFilterFactory(List<HeaderContributor> providers) {
        super(NameConfig.class);
        this.providers = providers;
    }

    /**
     * Defines the order of configuration fields for shortcut configuration in
     * {@code application.yaml}.
     *
     * @return the ordered list of configuration field names
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(NAME_KEY);
    }

    /**
     * Creates a new {@link GatewayFilter} instance with the configured providers.
     *
     * @param config the filter configuration
     * @return the configured {@link GatewayFilter}
     */
    @Override
    public GatewayFilter apply(NameConfig config) {
        return new AddSecHeadersGatewayFilter(providers);
    }

    /**
     * {@link GatewayFilter} implementation that applies the configured security
     * headers to proxied requests.
     */
    @RequiredArgsConstructor
    private static class AddSecHeadersGatewayFilter implements GatewayFilter, Ordered {

        private final @NonNull List<HeaderContributor> providers;

        /**
         * Applies the configured security headers to the request unless the
         * {@link #DISABLE_SECURITY_HEADERS} attribute is present.
         *
         * @param exchange the current server exchange
         * @param chain    the gateway filter chain
         * @return a {@link Mono} that proceeds with the filter chain execution
         */
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (exchange.getAttribute(DISABLE_SECURITY_HEADERS) == null) {
                ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                providers.stream().map(provider -> provider.prepare(exchange)).forEach(requestBuilder::headers);

                ServerHttpRequest request = requestBuilder.build();
                ServerWebExchange updatedExchange = exchange.mutate().request(request).build();
                return chain.filter(updatedExchange);
            }
            return chain.filter(exchange);
        }

        /**
         * Specifies the execution order of this filter to run immediately after
         * {@link ResolveTargetGlobalFilter}.
         *
         * @return the execution order of this filter
         */
        @Override
        public int getOrder() {
            return ResolveTargetGlobalFilter.ORDER + 1;
        }
    }
}
