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
package org.georchestra.gateway.model;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.server.ServerWebExchange;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * Represents the security and HTTP request header settings for a matched
 * {@link Route}.
 * <p>
 * This class defines role-based access rules and headers to be applied to
 * proxied requests for a given route.
 * </p>
 */
@Data
@Generated
@Accessors(fluent = true, chain = true)
public class GeorchestraTargetConfig {

    /**
     * Attribute key used to store the target configuration in the exchange.
     */
    private static final String TARGET_CONFIG_KEY = GeorchestraTargetConfig.class.getCanonicalName() + ".target";

    /**
     * HTTP request headers to append when forwarding requests.
     */
    private HeaderMappings headers;

    /**
     * Role-based access rules for controlling request authorization.
     */
    private List<RoleBasedAccessRule> accessRules;

    /**
     * Retrieves the stored {@link GeorchestraTargetConfig} from the exchange, if
     * available.
     *
     * @param exchange the {@link ServerWebExchange} containing the attributes
     * @return an {@link Optional} containing the stored target configuration, or
     *         empty if none exists
     */
    public static Optional<GeorchestraTargetConfig> getTarget(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getAttributes().get(TARGET_CONFIG_KEY))
                .map(GeorchestraTargetConfig.class::cast);
    }

    /**
     * Stores a {@link GeorchestraTargetConfig} instance in the exchange attributes.
     *
     * @param exchange the {@link ServerWebExchange} where the configuration should
     *                 be stored
     * @param config   the {@link GeorchestraTargetConfig} instance to store
     */
    public static void setTarget(ServerWebExchange exchange, GeorchestraTargetConfig config) {
        exchange.getAttributes().put(TARGET_CONFIG_KEY, config);
    }
}
