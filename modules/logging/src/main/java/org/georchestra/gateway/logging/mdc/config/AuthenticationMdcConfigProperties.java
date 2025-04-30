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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.logging.mdc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for controlling which authentication information is
 * included in the MDC.
 * <p>
 * These properties determine what user and authentication-related information
 * is added to the MDC (Mapped Diagnostic Context) during request processing.
 * Including this information in the MDC makes it available to all logging
 * statements, providing valuable context for debugging, monitoring, and audit
 * purposes.
 * <p>
 * The properties are configured using the prefix
 * {@code logging.mdc.include.user} in the application properties or YAML files.
 * <p>
 * Example configuration in YAML:
 * 
 * <pre>
 * logging:
 *   mdc:
 *     include:
 *       user:
 *         id: true
 *         roles: true
 *         org: true
 *         auth-method: true
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "logging.mdc.include.user")
public class AuthenticationMdcConfigProperties {

    /**
     * Whether to append the enduser.id and enduser.uuid MDC properties for the
     * authenticated user.
     */
    private boolean id = true;

    /** Whether to append the enduser.roles MDC property containing user roles. */
    private boolean roles = false;

    /**
     * Whether to append the enduser.org.id and enduser.org.uuid MDC properties
     * representing the user's organization.
     */
    private boolean org = false;

    /**
     * Whether to append extra MDC properties for the authenticated user:
     * enduser.firstname, enduser.lastname, enduser.org.fullname
     */
    private boolean extras = false;

    /**
     * Whether to append the enduser.auth-method MDC property for the authentication
     * method used.
     */
    private boolean authMethod = false;
}