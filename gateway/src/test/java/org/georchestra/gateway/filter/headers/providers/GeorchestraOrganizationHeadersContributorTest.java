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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.georchestra.gateway.filter.headers.HeaderContributor;
import org.georchestra.gateway.model.GeorchestraOrganizations;
import org.georchestra.gateway.model.GeorchestraTargetConfig;
import org.georchestra.gateway.model.HeaderMappings;
import org.georchestra.security.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * Test suite for the {@link GeorchestraOrganizationHeadersContributor}
 * {@link HeaderContributor}
 *
 */
class GeorchestraOrganizationHeadersContributorTest {

    GeorchestraOrganizationHeadersContributor headerContributor;
    ServerWebExchange exchange;
    HeaderMappings matchedRouteHeadersConfig;

    @BeforeEach
    void init() {
        headerContributor = new GeorchestraOrganizationHeadersContributor();
        matchedRouteHeadersConfig = new HeaderMappings();
        GeorchestraTargetConfig matchedRouteConfig = new GeorchestraTargetConfig().headers(matchedRouteHeadersConfig);

        exchange = mock(ServerWebExchange.class);
        Map<String, Object> exchangeAttributes = new HashMap<>();
        when(exchange.getAttributes()).thenReturn(exchangeAttributes);

        GeorchestraTargetConfig.setTarget(exchange, matchedRouteConfig);
    }

    @Test
    void testNoMatchedRouteConfig() {
        GeorchestraTargetConfig.setTarget(exchange, null);
        assertTrue(GeorchestraTargetConfig.getTarget(exchange).isEmpty());

        Consumer<HttpHeaders> contributor = headerContributor.prepare(exchange);
        assertNotNull(contributor);

        HttpHeaders target = new HttpHeaders();
        contributor.accept(target);
        assertTrue(target.isEmpty());
    }

    @Test
    void testNoOrganization() {
        Consumer<HttpHeaders> contributor = headerContributor.prepare(exchange);
        assertNotNull(contributor);

        HttpHeaders target = new HttpHeaders();
        contributor.accept(target);
        assertTrue(target.isEmpty());
    }

    @Test
    void testContributesHeadersFromOrganization() {
        Organization org = new Organization();
        org.setId("abc");
        org.setName("PSC");
        org.setLastUpdated("123");

        GeorchestraOrganizations.store(exchange, org);

        matchedRouteHeadersConfig.enableAll();

        Consumer<HttpHeaders> contributor = headerContributor.prepare(exchange);
        assertNotNull(contributor);

        HttpHeaders target = new HttpHeaders();
        contributor.accept(target);

        assertEquals(List.of(org.getId()), target.get("sec-orgid"));
        assertEquals(List.of(org.getName()), target.get("sec-orgname"));
        assertEquals(List.of(org.getLastUpdated()), target.get("sec-org-lastupdated"));
    }
}
