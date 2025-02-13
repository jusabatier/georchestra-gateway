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
package org.georchestra.gateway.filter.headers;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;

/**
 * A geOrchestra-specific {@link GatewayFilterFactory} that removes all incoming
 * security-related request headers, preventing unauthorized impersonation of
 * authenticated users.
 * <p>
 * This filter is designed to strip:
 * </p>
 * <ul>
 * <li>All headers prefixed with {@code sec-*}, which geOrchestra uses for
 * user-related security information.</li>
 * <li>The {@code Authorization} header when it contains Basic authentication
 * credentials.</li>
 * </ul>
 * <p>
 * By removing these headers from incoming requests, the gateway ensures that
 * authentication and authorization are enforced properly and prevents external
 * clients from injecting unauthorized credentials.
 * </p>
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *    gateway:
 *      routes:
 *      - id: root
 *        uri: http://backend-service/context
 *        filters:
 *        - RemoveSecurityHeaders
 * </code>
 * </pre>
 * 
 * @see RemoveHeadersGatewayFilterFactory
 */
public class RemoveSecurityHeadersGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final String DEFAULT_SEC_HEADERS_PATTERN = "(?i)(sec-.*|Authorization)";

    private final RemoveHeadersGatewayFilterFactory delegate;
    private final RemoveHeadersGatewayFilterFactory.RegExConfig config = new RemoveHeadersGatewayFilterFactory.RegExConfig(
            DEFAULT_SEC_HEADERS_PATTERN);

    /**
     * Creates a new instance of {@code RemoveSecurityHeadersGatewayFilterFactory}
     * that removes security-sensitive headers from incoming requests.
     */
    public RemoveSecurityHeadersGatewayFilterFactory() {
        super(Object.class);
        delegate = new RemoveHeadersGatewayFilterFactory();
    }

    /**
     * Applies the filter by delegating to {@link RemoveHeadersGatewayFilterFactory}
     * with a pre-configured regular expression that matches security-related
     * headers.
     * 
     * @param unused the configuration object (not used)
     * @return a {@link GatewayFilter} instance that removes security headers
     */
    @Override
    public GatewayFilter apply(Object unused) {
        return delegate.apply(config);
    }
}
