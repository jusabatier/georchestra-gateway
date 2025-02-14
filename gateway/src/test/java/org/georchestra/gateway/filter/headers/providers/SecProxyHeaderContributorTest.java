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

package org.georchestra.gateway.filter.headers.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * Test suite for {@link SecProxyHeaderContributor}
 *
 */
class SecProxyHeaderContributorTest {

    @Test
    void test() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        Consumer<HttpHeaders> consumer = new SecProxyHeaderContributor(() -> true).prepare(exchange);
        assertNotNull(consumer);
        HttpHeaders headers = new HttpHeaders();
        consumer.accept(headers);
        assertEquals(List.of("true"), headers.get("sec-proxy"));

        consumer = new SecProxyHeaderContributor(() -> false).prepare(exchange);
        assertNotNull(consumer);
        headers = new HttpHeaders();
        consumer.accept(headers);
        assertNull(headers.get("sec-proxy"));
    }

}
