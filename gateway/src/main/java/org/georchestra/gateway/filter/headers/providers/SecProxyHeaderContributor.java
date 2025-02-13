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
 * Contributes the {@literal sec-proxy: true} request header based on the value
 * returned by the provided {@link BooleanSupplier}, which is required by all
 * backend services as a flag indicating the request is authenticated.
 */
@RequiredArgsConstructor
public class SecProxyHeaderContributor extends HeaderContributor {

    private final @NonNull BooleanSupplier secProxyHeaderEnabled;

    public @Override Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> {
            if (secProxyHeaderEnabled.getAsBoolean())
                add(headers, "sec-proxy", "true");
        };
    }
}
