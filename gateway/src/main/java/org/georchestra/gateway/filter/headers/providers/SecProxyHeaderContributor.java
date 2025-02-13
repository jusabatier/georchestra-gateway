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
package org.georchestra.gateway.filter.headers.providers;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.georchestra.gateway.filter.headers.HeaderContributor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link HeaderContributor} that appends the {@code sec-proxy: true} request
 * header to indicate that the request is authenticated through the gateway.
 * <p>
 * This header is required by all backend services to differentiate between
 * authenticated and unauthenticated requests.
 * </p>
 * <p>
 * The contribution of this header is controlled by a {@link BooleanSupplier},
 * allowing dynamic enablement based on external conditions.
 * </p>
 *
 * @see HeaderContributor
 */
@RequiredArgsConstructor
public class SecProxyHeaderContributor extends HeaderContributor {

    private final @NonNull BooleanSupplier secProxyHeaderEnabled;

    /**
     * Prepares a header contributor that appends the {@code sec-proxy} header if
     * enabled.
     *
     * @param exchange the current {@link ServerWebExchange}
     * @return a {@link Consumer} that modifies the request headers
     */
    public @Override Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> {
            if (secProxyHeaderEnabled.getAsBoolean()) {
                add(headers, "sec-proxy", "true");
            }
        };
    }
}
