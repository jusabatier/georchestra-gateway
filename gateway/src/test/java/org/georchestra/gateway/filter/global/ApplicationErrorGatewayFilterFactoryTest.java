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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;
import java.util.List;

import org.georchestra.gateway.model.HeaderMappings;
import org.georchestra.gateway.model.RoleBasedAccessRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

class ApplicationErrorGatewayFilterFactoryTest {

    private GatewayFilter filter;
    private MockServerWebExchange exchange;

    final URI matchedURI = URI.create("http://fake.backend.com:8080");
    private Route matchedRoute;

    HeaderMappings defaultHeaders;
    List<RoleBasedAccessRule> defaultRules;

    @BeforeEach
    void setUp() throws Exception {
        var factory = new ApplicationErrorGatewayFilterFactory();
        filter = factory.apply(factory.newConfig());

        matchedRoute = mock(Route.class);
        when(matchedRoute.getUri()).thenReturn(matchedURI);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, matchedRoute);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, matchedURI);

    }

    @Test
    void testNotAnErrorResponse() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange mutated = captor.getValue();
        ServerHttpResponse response = mutated.getResponse();
        response.setStatusCode(HttpStatus.CREATED);

        MockServerHttpResponse origResponse = exchange.getResponse();
        assertThat(origResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test4xx() {
        testApplicationError(HttpStatus.BAD_REQUEST);
        testApplicationError(HttpStatus.UNAUTHORIZED);
        testApplicationError(HttpStatus.FORBIDDEN);
        testApplicationError(HttpStatus.NOT_FOUND);
    }

    @Test
    void test5xx() {
        testApplicationError(HttpStatus.INTERNAL_SERVER_ERROR);
        testApplicationError(HttpStatus.SERVICE_UNAVAILABLE);
        testApplicationError(HttpStatus.BAD_GATEWAY);
    }

    private void testApplicationError(HttpStatus status) {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        filter.filter(exchange, chain);
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange mutated = captor.getValue();
        ServerHttpResponse response = mutated.getResponse();
        assertThrows(ResponseStatusException.class, () -> response.setStatusCode(status));
    }
}
