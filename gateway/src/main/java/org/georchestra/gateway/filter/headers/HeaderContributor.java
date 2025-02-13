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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.georchestra.gateway.filter.headers.providers.GeorchestraOrganizationHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.GeorchestraUserHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.JsonPayloadHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.SecProxyHeaderContributor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy interface for contributing HTTP request headers to proxied requests.
 * <p>
 * Implementations of this class define specific security headers that should be
 * appended to proxied requests based on authentication and request context.
 * </p>
 * <p>
 * These implementations are used by {@link AddSecHeadersGatewayFilterFactory}
 * to determine which headers should be added.
 * </p>
 *
 * <h3>Implementations</h3>
 * <ul>
 * <li>{@link SecProxyHeaderContributor}: Appends the {@code sec-proxy}
 * header</li>
 * <li>{@link GeorchestraUserHeadersContributor}: Adds user-related security
 * headers</li>
 * <li>{@link GeorchestraOrganizationHeadersContributor}: Appends organization
 * information headers</li>
 * <li>{@link JsonPayloadHeadersContributor}: Encodes security attributes as a
 * JSON payload</li>
 * </ul>
 *
 * @see AddSecHeadersGatewayFilterFactory
 */
@Slf4j(topic = "org.georchestra.gateway.filter.headers")
public abstract class HeaderContributor implements Ordered {

    /**
     * Prepares a consumer that modifies {@link HttpHeaders} for a proxied request.
     * <p>
     * Implementations should return a consumer that either sets or appends headers
     * based on the security context and request attributes.
     * </p>
     *
     * @param exchange the current {@link ServerWebExchange}
     * @return a {@link Consumer} that modifies the request headers
     */
    public abstract Consumer<HttpHeaders> prepare(ServerWebExchange exchange);

    /**
     * {@inheritDoc}
     *
     * @return {@code 0} as the default order. Implementations may override this
     *         method to control execution order when multiple contributors are
     *         applied.
     * @see Ordered#HIGHEST_PRECEDENCE
     * @see Ordered#LOWEST_PRECEDENCE
     */
    public @Override int getOrder() {
        return 0;
    }

    /**
     * Appends a header to the request if it is enabled and has a valid value.
     *
     * @param target  the target {@link HttpHeaders}
     * @param header  the header name
     * @param enabled whether the header should be included
     * @param value   the header value
     */
    protected void add(@NonNull HttpHeaders target, @NonNull String header, @NonNull Optional<Boolean> enabled,
            @NonNull Optional<String> value) {
        add(target, header, enabled, value.orElse(null));
    }

    /**
     * Appends a header to the request if it is enabled and has a valid list of
     * values.
     *
     * @param target  the target {@link HttpHeaders}
     * @param header  the header name
     * @param enabled whether the header should be included
     * @param values  the list of header values
     */
    protected void add(@NonNull HttpHeaders target, @NonNull String header, @NonNull Optional<Boolean> enabled,
            @NonNull List<String> values) {
        String val = values.isEmpty() ? null : values.stream().collect(Collectors.joining(";"));
        add(target, header, enabled, val);
    }

    /**
     * Appends a header to the request if it is enabled and has a valid value.
     *
     * @param target  the target {@link HttpHeaders}
     * @param header  the header name
     * @param enabled whether the header should be included
     * @param value   the header value
     */
    protected void add(@NonNull HttpHeaders target, @NonNull String header, @NonNull Optional<Boolean> enabled,
            String value) {
        if (enabled.orElse(Boolean.FALSE)) {
            if (value == null) {
                log.trace("Value for header {} is not present", header);
            } else {
                log.debug("Appending header {}: {}", header, value);
                target.add(header, value);
            }
        } else {
            log.trace("Header {} is not enabled", header);
        }
    }

    /**
     * Appends a header to the request if it has a valid value.
     *
     * @param target the target {@link HttpHeaders}
     * @param header the header name
     * @param value  the header value
     */
    protected void add(@NonNull HttpHeaders target, @NonNull String header, String value) {
        if (value == null) {
            log.trace("Value for header {} is not present", header);
        } else {
            log.debug("Appending header {}: {}", header, value);
            target.add(header, value);
        }
    }
}
