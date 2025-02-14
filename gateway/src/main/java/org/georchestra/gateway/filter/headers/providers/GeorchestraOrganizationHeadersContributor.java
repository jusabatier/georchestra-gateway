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

import java.util.Optional;
import java.util.function.Consumer;

import org.georchestra.gateway.filter.headers.HeaderContributor;
import org.georchestra.gateway.model.GeorchestraOrganizations;
import org.georchestra.gateway.model.GeorchestraTargetConfig;
import org.georchestra.security.model.Organization;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * {@link HeaderContributor} that appends organization-related security headers
 * to proxied requests.
 * <p>
 * This contributor extracts organization information from the current request
 * context and applies the configured security headers based on
 * {@link GeorchestraTargetConfig}.
 * </p>
 *
 * <h3>Appended Headers</h3>
 * <ul>
 * <li>{@code sec-orgname} - Organization name</li>
 * <li>{@code sec-orgid} - Organization ID</li>
 * <li>{@code sec-org-lastupdated} - Last updated timestamp of the
 * organization</li>
 * </ul>
 */
public class GeorchestraOrganizationHeadersContributor extends HeaderContributor {

    /**
     * Prepares a header contributor that appends organization-related security
     * headers to the request.
     * <p>
     * Headers are only added if the organization is resolved from the request and
     * the corresponding configuration enables them.
     * </p>
     *
     * @param exchange the current {@link ServerWebExchange}
     * @return a {@link Consumer} that modifies the request headers
     */
    public @Override Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> {
            GeorchestraTargetConfig.getTarget(exchange).map(GeorchestraTargetConfig::headers).ifPresent(mappings -> {
                Optional<Organization> org = GeorchestraOrganizations.resolve(exchange);
                add(headers, "sec-orgname", mappings.getOrgname(), org.map(Organization::getName));
                add(headers, "sec-orgid", mappings.getOrgid(), org.map(Organization::getId));
                add(headers, "sec-org-lastupdated", mappings.getOrgid(), org.map(Organization::getLastUpdated));
            });
        };
    }
}
