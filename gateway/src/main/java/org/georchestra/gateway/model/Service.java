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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.Generated;

/**
 * Represents the configuration of a backend service within the geOrchestra
 * Gateway.
 * <p>
 * This model defines the target service URL, role-based access rules, and
 * security headers to be applied to proxied requests.
 * </p>
 */
@Data
@Generated
public class Service {

    /**
     * The backend service URL to which the gateway will proxy incoming requests.
     * <p>
     * The routing is determined based on the {@link #getAccessRules() access rules}
     * and their associated {@link RoleBasedAccessRule#getInterceptUrl()
     * intercept-URLs}.
     * </p>
     */
    private URI target;

    /**
     * Service-specific security headers configuration.
     * <p>
     * These headers will be appended to requests forwarded to the backend service.
     * </p>
     */
    private HeaderMappings headers;

    /**
     * List of Ant-pattern based access rules for controlling access to the backend
     * service.
     */
    private List<RoleBasedAccessRule> accessRules = List.of();

    /**
     * Retrieves the optional security headers configuration for this service.
     *
     * @return an {@link Optional} containing the {@link HeaderMappings}, or empty
     *         if not defined
     */
    public Optional<HeaderMappings> headers() {
        return Optional.ofNullable(headers);
    }
}
