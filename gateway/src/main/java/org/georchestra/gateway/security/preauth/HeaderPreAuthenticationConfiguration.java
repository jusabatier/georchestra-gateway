/*
 * Copyright (C) 2023 by the geOrchestra PSC
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

import org.georchestra.gateway.security.GeorchestraUserMapper;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Configuration class for enabling request-header-based pre-authentication.
 * <p>
 * This setup allows authentication to be performed via HTTP request headers,
 * typically injected by a trusted reverse proxy or identity provider.
 * Authentication is only considered valid if the
 * {@code sec-georchestra-preauthenticated} header is present and set to
 * {@code true}.
 * </p>
 *
 * <h3>Authentication Flow:</h3>
 * <ul>
 * <li>{@link PreauthGatewaySecurityCustomizer}:
 * <ul>
 * <li>Intercepts incoming requests and extracts pre-authentication
 * headers.</li>
 * <li>Creates a {@link PreAuthenticatedAuthenticationToken} if authentication
 * is valid.</li>
 * <li>Ensures that client requests cannot tamper with {@code sec-*}
 * headers.</li>
 * </ul>
 * </li>
 * <li>{@link PreauthenticatedUserMapperExtension}:
 * <ul>
 * <li>Maps a {@link PreAuthenticatedAuthenticationToken} to a
 * {@link GeorchestraUser}.</li>
 * <li>Used by {@link GeorchestraUserMapper} when resolving authentication.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Expected Headers:</h3> The following HTTP headers can be used for
 * authentication:
 * <ul>
 * <li>{@code preauth-username} - Username of the authenticated user.</li>
 * <li>{@code preauth-firstname} - User's first name.</li>
 * <li>{@code preauth-lastname} - User's last name.</li>
 * <li>{@code preauth-org} - Organization name.</li>
 * <li>{@code preauth-email} - Email address of the user.</li>
 * <li>{@code preauth-roles} - (Optional) Comma-separated list of user
 * roles.</li>
 * </ul>
 * <p>
 * <b>Note:</b> If {@code preauth-roles} is not provided, the user will only be
 * assigned the default role {@code ROLE_USER}.
 * </p>
 *
 * <h3>Example Configuration:</h3>
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
@Configuration
@EnableConfigurationProperties(HeaderPreauthConfigProperties.class)
public class HeaderPreAuthenticationConfiguration {

    /**
     * Registers a security customizer that enables authentication based on
     * pre-authentication headers.
     *
     * @return a {@link PreauthGatewaySecurityCustomizer} bean
     */
    @Bean
    PreauthGatewaySecurityCustomizer preauthGatewaySecurityCustomizer() {
        return new PreauthGatewaySecurityCustomizer();
    }

    /**
     * Registers a mapper that converts a
     * {@link PreAuthenticatedAuthenticationToken} into a {@link GeorchestraUser}
     * instance.
     *
     * @return a {@link PreauthenticatedUserMapperExtension} bean
     */
    @Bean
    PreauthenticatedUserMapperExtension preauthenticatedUserMapperExtension() {
        return new PreauthenticatedUserMapperExtension();
    }
}
