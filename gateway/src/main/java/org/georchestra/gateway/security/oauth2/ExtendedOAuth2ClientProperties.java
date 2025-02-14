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
package org.georchestra.gateway.security.oauth2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Extended version of {@link OAuth2ClientProperties} to support additional
 * OAuth2 provider configurations, specifically adding an end session URI for
 * logout handling.
 * <p>
 * This class allows defining extra properties under the
 * `spring.security.oauth2.client` namespace, enabling seamless integration with
 * OAuth2 providers that support session termination via a dedicated logout
 * endpoint.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 * 
 * <pre>
 * {@code
 * spring:
 *   security:
 *     oauth2:
 *       client:
 *         provider:
 *           keycloak:
 *             issuer-uri: https://keycloak.example.com/realms/myrealm
 *             authorization-uri: https://keycloak.example.com/auth
 *             token-uri: https://keycloak.example.com/token
 *             end-session-uri: https://keycloak.example.com/logout
 * }
 * </pre>
 *
 * <p>
 * This allows retrieving the end session URI via:
 * </p>
 * 
 * <pre>
 * {
 *     &#64;code
 *     String logoutUrl = extendedOAuth2ClientProperties.getProvider().get("keycloak").getEndSessionUri();
 * }
 * </pre>
 *
 * @see OAuth2ClientProperties
 */
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class ExtendedOAuth2ClientProperties {

    private final Map<String, Provider> provider = new HashMap<>();

    /**
     * Retrieves the map of configured OAuth2 providers.
     *
     * @return a map where the key is the provider name, and the value contains its
     *         configuration.
     */
    public Map<String, Provider> getProvider() {
        return this.provider;
    }

    /**
     * Represents an extended OAuth2 provider configuration, adding support for an
     * end session URI to handle provider-specific logout functionality.
     */
    public static class Provider extends OAuth2ClientProperties.Provider {

        private String endSessionUri;

        /**
         * Retrieves the provider's end session URI, used for logging out the user.
         *
         * @return the end session URI, or {@code null} if not configured.
         */
        public String getEndSessionUri() {
            return this.endSessionUri;
        }

        /**
         * Sets the provider's end session URI.
         *
         * @param endSessionUri the logout endpoint of the OAuth2 provider.
         */
        public void setEndSessionUri(String endSessionUri) {
            this.endSessionUri = endSessionUri;
        }
    }
}
