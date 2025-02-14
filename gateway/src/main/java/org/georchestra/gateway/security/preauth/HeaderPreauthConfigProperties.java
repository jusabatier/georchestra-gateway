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
package org.georchestra.gateway.security.preauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.Generated;

/**
 * Configuration properties for enabling and configuring request-header-based
 * pre-authentication.
 * <p>
 * When enabled, authentication can be performed by sending a special header
 * ({@code sec-georchestra-preauthenticated}) set to {@code true}, along with
 * additional user identity details in the following request headers:
 * <ul>
 * <li>{@code preauth-username} - The username of the pre-authenticated
 * user.</li>
 * <li>{@code preauth-firstname} - The first name of the user.</li>
 * <li>{@code preauth-lastname} - The last name of the user.</li>
 * <li>{@code preauth-org} - The organization of the user.</li>
 * <li>{@code preauth-email} - The user's email address.</li>
 * <li>{@code preauth-roles} - A comma-separated list of roles assigned to the
 * user.</li>
 * </ul>
 * <p>
 * This mechanism allows an external authentication system (e.g., a reverse
 * proxy or another identity provider) to inject user identity information into
 * requests without requiring direct authentication within the application.
 * 
 * <p>
 * Example configuration in {@code application.yml}:
 * 
 * <pre>
 * {@code
 * georchestra:
 *   gateway:
 *     security:
 *       header-authentication:
 *         enabled: true
 * }
 * </pre>
 */
@Data
@Generated
@ConfigurationProperties(HeaderPreauthConfigProperties.PROPERTY_BASE)
public class HeaderPreauthConfigProperties {

    /** Base property prefix for header authentication settings. */
    static final String PROPERTY_BASE = "georchestra.gateway.security.header-authentication";

    /** Property key for enabling header-based pre-authentication. */
    public static final String ENABLED_PROPERTY = PROPERTY_BASE + ".enabled";

    /**
     * Whether header-based pre-authentication is enabled.
     * <p>
     * When {@code true}, authentication via request headers is allowed.
     */
    private boolean enabled = false;
}
