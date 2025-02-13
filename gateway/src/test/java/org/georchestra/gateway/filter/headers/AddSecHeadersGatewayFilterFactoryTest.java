/*
 * Copyright (C) 2022 by the geOrchestra PSC
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory.NameConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

/**
 * Test suite for {@link AddSecHeadersGatewayFilterFactory}
 *
 */
class AddSecHeadersGatewayFilterFactoryTest {

    private AddSecHeadersGatewayFilterFactory factory;
    private List<HeaderContributor> providers;

    private GatewayFilterChain mockChain;

    @BeforeEach
    void setUp() throws Exception {
        providers = new ArrayList<>();
        factory = new AddSecHeadersGatewayFilterFactory(providers);

        mockChain = mock(GatewayFilterChain.class);
    }

    @Test
    void test() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        HeaderContributor extension1 = mock(HeaderContributor.class);
        HeaderContributor extension2 = mock(HeaderContributor.class);
        Consumer<HttpHeaders> consumer1 = headers -> headers.add("header-from-extension1", "true");
        Consumer<HttpHeaders> consumer2 = headers -> headers.add("header-from-extension2", "true");
        when(extension1.prepare(any())).thenReturn(consumer1);
        when(extension2.prepare(any())).thenReturn(consumer2);

        providers.add(extension1);
        providers.add(extension2);

        GatewayFilter filter = factory.apply((NameConfig) null);
        filter.filter(exchange, mockChain);

        ArgumentCaptor<ServerWebExchange> mutatedExchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(mockChain, times(1)).filter(mutatedExchangeCaptor.capture());
        verify(extension1, times(1)).prepare(same(exchange));
        verify(extension2, times(1)).prepare(same(exchange));

        ServerWebExchange mutatedExchange = mutatedExchangeCaptor.getValue();
        assertNotSame(exchange, mutatedExchange);
        HttpHeaders finalHeaders = mutatedExchange.getRequest().getHeaders();
        assertEquals("true", finalHeaders.toSingleValueMap().get("header-from-extension1"));
        assertEquals("true", finalHeaders.toSingleValueMap().get("header-from-extension2"));
    }

}
