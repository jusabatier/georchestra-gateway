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
package org.georchestra.gateway.model;

import java.util.Optional;

import org.georchestra.security.model.Organization;
import org.springframework.web.server.ServerWebExchange;

import lombok.experimental.UtilityClass;

/**
 * Utility class for handling geOrchestra organization attributes within a
 * {@link ServerWebExchange}.
 * <p>
 * This class provides methods to store and retrieve an {@link Organization}
 * instance associated with an exchange.
 * </p>
 */
@UtilityClass
public class GeorchestraOrganizations {

    /**
     * Attribute key used to store the organization in the exchange.
     */
    static final String GEORCHESTRA_ORGANIZATION_KEY = GeorchestraOrganizations.class.getCanonicalName();

    /**
     * Retrieves the stored {@link Organization} from the exchange, if available.
     *
     * @param exchange the {@link ServerWebExchange} containing the attributes
     * @return an {@link Optional} containing the stored organization, or empty if
     *         none exists
     */
    public static Optional<Organization> resolve(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getAttributes().get(GEORCHESTRA_ORGANIZATION_KEY))
                .map(Organization.class::cast);
    }

    /**
     * Stores an {@link Organization} instance in the exchange attributes.
     *
     * @param exchange the {@link ServerWebExchange} where the organization should
     *                 be stored
     * @param org      the {@link Organization} instance to store
     */
    public static void store(ServerWebExchange exchange, Organization org) {
        exchange.getAttributes().put(GEORCHESTRA_ORGANIZATION_KEY, org);
    }
}
