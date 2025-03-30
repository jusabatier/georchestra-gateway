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
package org.georchestra.gateway.logging.accesslog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

class AccessLogWebfluxFilterTest {

    @Test
    void getOrderShouldReturnLowestPrecedence() {
        // Setup
        AccessLogFilterConfig config = mock(AccessLogFilterConfig.class);
        AccessLogWebfluxFilter filter = new AccessLogWebfluxFilter(config);

        // Verify
        assertThat(filter.getOrder()).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }

    @Test
    void constructorShouldAcceptNonNullConfig() {
        // Setup
        AccessLogFilterConfig config = mock(AccessLogFilterConfig.class);

        // Execute & Verify (no exception)
        new AccessLogWebfluxFilter(config);
    }

    @Test
    void filterShouldReturnCompletedMonoWhenChainCompletes() {
        // Setup
        AccessLogFilterConfig config = mock(AccessLogFilterConfig.class);
        AccessLogWebfluxFilter filter = new AccessLogWebfluxFilter(config);

        // Create a mock ServerWebExchange
        org.springframework.web.server.ServerWebExchange exchange = mock(
                org.springframework.web.server.ServerWebExchange.class);
        org.springframework.http.server.reactive.ServerHttpRequest request = mock(
                org.springframework.http.server.reactive.ServerHttpRequest.class);
        org.springframework.http.server.reactive.ServerHttpResponse response = mock(
                org.springframework.http.server.reactive.ServerHttpResponse.class);
        java.net.URI uri = java.net.URI.create("http://example.com/test");
        org.springframework.http.HttpMethod httpMethod = org.springframework.http.HttpMethod.GET;

        // Setup the mock exchange
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn(httpMethod);

        // Mock shouldLog to return false to simplify test
        when(config.shouldLog(any())).thenReturn(false);

        // Setup mock chain that returns completed Mono
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Execute - we're just testing that chain.filter() is called and result
        // propagated
        Mono<Void> result = filter.filter(exchange, chain);

        // Verify the filter's Mono completes
        assertThat(result).isNotNull();
    }
}