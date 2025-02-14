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
package org.georchestra.gateway.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for the OAuth2 client HTTP proxy.
 * <p>
 * This configuration allows the OAuth2 client to use a proxy when communicating
 * with the authentication provider, which can be useful in environments with
 * restricted network access.
 * </p>
 *
 * <p>
 * Example configuration in {@code application.yml}:
 * </p>
 * 
 * <pre>
 * <code>
 * georchestra:
 *   gateway:
 *     security:
 *       oauth2:
 *         proxy:
 *           enabled: true
 *           host: proxy.example.com
 *           port: 8080
 *           username: proxyuser
 *           password: proxypass
 * </code>
 * </pre>
 */
@ConfigurationProperties(prefix = "georchestra.gateway.security.oauth2.proxy")
@Data
public class OAuth2ProxyConfigProperties {

    /**
     * Whether the OAuth2 client should use an HTTP proxy.
     */
    private boolean enabled;

    /**
     * The proxy host address (e.g., {@code proxy.example.com}).
     */
    private String host;

    /**
     * The proxy port number.
     */
    private Integer port;

    /**
     * The optional proxy username for authentication.
     */
    private String username;

    /**
     * The optional proxy password for authentication.
     */
    private String password;
}
