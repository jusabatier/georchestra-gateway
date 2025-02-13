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
 * The HTTP request headers and role-based access rules of a matched
 * {@link Route}
 */
@Data
@Generated
@Accessors(fluent = true, chain = true)
public class GeorchestraTargetConfig {

    private static final String TARGET_CONFIG_KEY = GeorchestraTargetConfig.class.getCanonicalName() + ".target";

    private HeaderMappings headers;
    private List<RoleBasedAccessRule> accessRules;

    public static Optional<GeorchestraTargetConfig> getTarget(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getAttributes().get(TARGET_CONFIG_KEY))
                .map(GeorchestraTargetConfig.class::cast);
    }

    public static void setTarget(ServerWebExchange exchange, GeorchestraTargetConfig config) {
        exchange.getAttributes().put(TARGET_CONFIG_KEY, config);
    }
}
