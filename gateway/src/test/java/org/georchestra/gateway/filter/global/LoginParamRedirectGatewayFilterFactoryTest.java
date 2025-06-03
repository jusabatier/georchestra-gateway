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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.gateway.filter.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.georchestra.gateway.filter.global.LoginParamRedirectGatewayFilterFactory.LoginParamRedirectGatewayFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Verify the {@link LoginParamRedirectGatewayFilterFactory} redirects to
 * {@code /login} when a {@code ?login} query parameter is present in a request
 * that's not already authenticated
 */
class LoginParamRedirectGatewayFilterFactoryTest {

    LoginParamRedirectGatewayFilter filter;
    ServerWebExchange exchange;
    MockServerHttpRequest request;
    GatewayFilterChain chain;

    @BeforeEach
    void before() {
        filter = new LoginParamRedirectGatewayFilterFactory() //
                .setRedirectServerAuthenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/login"))
                .apply(/* unused, filter has config class */ new Object());
        request = MockServerHttpRequest.get("/test/?login").build();

        exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.I_AM_A_TEAPOT);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("when no login query param is given, then continue with the filter chain")
    void ifNoLoginParamThenPassThruChain() {
        // no ?login query param
        request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.I_AM_A_TEAPOT);

        filter.filter(exchange, chain).block();
        verify(chain, times(1)).filter(exchange);

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
    }

    @Test
    @DisplayName("given the request is already authenticated, then the login parameter is ignored")
    void ifLoginParamAndAlreadyAuthorizedThenPassThruChain() {
        login();
        filter.filter(exchange, chain).block();
        verify(chain, times(1)).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode())
                .as("Expected I_AM_A_TEAPOT since the reques is already authrized").isEqualTo(HttpStatus.I_AM_A_TEAPOT);
    }

    @Test
    @DisplayName("given the request is not authenticated and has a login parameter, then the response is redirected to /login")
    void ifLoginParamAndNotAlreadyAuthorizedThenRedirect() {
        filter.filter(exchange, chain).block();
        verify(chain, never()).filter(exchange);

        assertThat(exchange.getResponse().getStatusCode())
                .as("Expected 302 Found since the request has ?login and is not pre-authrized")
                .isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders()).containsEntry("Location", List.of("/login"));
    }

    private void login() {
        Authentication auth = new TestingAuthenticationToken("testuser", null, "ROLE_USER");
        filter = spy(filter);
        when(filter.getAuthentication()).thenReturn(Mono.just(auth));
    }

}
