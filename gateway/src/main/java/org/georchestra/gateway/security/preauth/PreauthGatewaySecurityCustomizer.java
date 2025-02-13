/*
 * Copyright (C) 2023 by the geOrchestra PSC
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
package org.georchestra.gateway.security.preauth;

import org.georchestra.gateway.security.ServerHttpSecurityCustomizer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Customizes {@link ServerHttpSecurity} to enable pre-authentication based on
 * HTTP request headers.
 * <p>
 * This security customizer sets up an authentication filter that extracts user
 * information from specific headers. If valid pre-authentication headers are
 * found, authentication is performed and an authenticated user is established
 * in the security context.
 * </p>
 *
 * <h3>Customization Steps:</h3>
 * <ol>
 * <li>Creates a {@link PreauthAuthenticationManager} to handle
 * authentication.</li>
 * <li>Registers an {@link AuthenticationWebFilter} to authenticate requests
 * with pre-auth headers.</li>
 * <li>Registers a {@link RemovePreauthHeadersWebFilter} to strip pre-auth
 * headers from downstream requests, preventing them from being misused by
 * backend services.</li>
 * </ol>
 *
 * <p>
 * The authentication process is initiated if the
 * {@code sec-georchestra-preauthenticated} header is present.
 * </p>
 */
public class PreauthGatewaySecurityCustomizer implements ServerHttpSecurityCustomizer {

    /**
     * Configures {@link ServerHttpSecurity} to add the pre-authentication filters.
     * <p>
     * This method does the following:
     * <ul>
     * <li>Creates an {@link AuthenticationWebFilter} with a
     * {@link PreauthAuthenticationManager}.</li>
     * <li>Sets the authentication converter to extract credentials from HTTP
     * headers.</li>
     * <li>Adds the authentication filter as the first filter in the security filter
     * chain.</li>
     * <li>Adds a post-processing filter to remove pre-authentication headers before
     * passing the request to downstream services.</li>
     * </ul>
     *
     * @param http the {@link ServerHttpSecurity} instance to configure.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void customize(ServerHttpSecurity http) {
        PreauthAuthenticationManager authenticationManager = new PreauthAuthenticationManager();
        AuthenticationWebFilter headerFilter = new AuthenticationWebFilter(authenticationManager);

        // Set the authentication converter to extract credentials from headers
        headerFilter.setAuthenticationConverter(authenticationManager::convert);

        // Add authentication filter at the beginning of the security filter chain
        http.addFilterAt(headerFilter, SecurityWebFiltersOrder.FIRST);

        // Add a filter at the end of the chain to remove pre-auth headers before
        // forwarding the request
        http.addFilterAt(new RemovePreauthHeadersWebFilter(authenticationManager), SecurityWebFiltersOrder.LAST);
    }

    /**
     * A {@link WebFilter} that removes pre-authentication headers from the request
     * before passing it to the next filter in the chain.
     * <p>
     * This ensures that backend services do not see or rely on the
     * pre-authentication headers, which could otherwise be misused.
     * </p>
     */
    @RequiredArgsConstructor
    static class RemovePreauthHeadersWebFilter implements WebFilter {

        private final PreauthAuthenticationManager manager;

        /**
         * Filters incoming requests by removing pre-authentication headers before
         * continuing the chain.
         *
         * @param exchange the {@link ServerWebExchange} representing the HTTP request
         *                 and response.
         * @param chain    the {@link WebFilterChain} to delegate further request
         *                 processing.
         * @return a {@link Mono<Void>} indicating when request processing is complete.
         */
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest().mutate().headers(manager::removePreauthHeaders).build();
            exchange = exchange.mutate().request(request).build();
            return chain.filter(exchange);
        }
    }
}
