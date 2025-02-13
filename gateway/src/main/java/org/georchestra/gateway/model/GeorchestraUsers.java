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

import java.util.Map;
import java.util.Optional;

import org.georchestra.security.model.GeorchestraUser;
import org.springframework.web.server.ServerWebExchange;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class for managing geOrchestra user attributes within a
 * {@link ServerWebExchange}.
 * <p>
 * This class provides methods to store and retrieve a {@link GeorchestraUser}
 * instance associated with an exchange.
 * </p>
 */
@UtilityClass
public class GeorchestraUsers {

    /**
     * Attribute key used to store the geOrchestra user in the exchange.
     */
    static final String GEORCHESTRA_USER_KEY = GeorchestraUsers.class.getCanonicalName();

    /**
     * Retrieves the stored {@link GeorchestraUser} from the exchange, if available.
     *
     * @param exchange the {@link ServerWebExchange} containing the attributes
     * @return an {@link Optional} containing the stored user, or empty if none
     *         exists
     */
    public static Optional<GeorchestraUser> resolve(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getAttributes().get(GEORCHESTRA_USER_KEY)).map(GeorchestraUser.class::cast);
    }

    /**
     * Stores a {@link GeorchestraUser} instance in the exchange attributes.
     * <p>
     * If the provided user is {@code null}, the attribute is removed.
     * </p>
     *
     * @param exchange the {@link ServerWebExchange} where the user should be stored
     * @param user     the {@link GeorchestraUser} instance to store, or
     *                 {@code null} to remove it
     * @return the updated {@link ServerWebExchange} instance
     */
    public static ServerWebExchange store(@NonNull ServerWebExchange exchange, GeorchestraUser user) {
        Map<String, Object> attributes = exchange.getAttributes();
        if (user == null) {
            attributes.remove(GEORCHESTRA_USER_KEY);
        } else {
            attributes.put(GEORCHESTRA_USER_KEY, user);
        }
        return exchange;
    }
}
