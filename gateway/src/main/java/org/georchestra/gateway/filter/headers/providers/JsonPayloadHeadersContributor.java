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

import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.gateway.filter.headers.HeaderContributor;
import org.georchestra.gateway.model.GeorchestraOrganizations;
import org.georchestra.gateway.model.GeorchestraTargetConfig;
import org.georchestra.gateway.model.GeorchestraUsers;
import org.georchestra.gateway.model.HeaderMappings;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * {@link HeaderContributor} that appends user and organization information as
 * Base64-encoded JSON payloads to proxied requests.
 * <p>
 * This contributor enables the following security headers based on the matched
 * route configuration:
 * </p>
 * <ul>
 * <li>{@code sec-user} - Contains a Base64-encoded JSON representation of the
 * authenticated {@link GeorchestraUser}.</li>
 * <li>{@code sec-organization} - Contains a Base64-encoded JSON representation
 * of the resolved {@link Organization}.</li>
 * </ul>
 * <p>
 * The encoding process ensures the data is included only when explicitly
 * enabled via {@link HeaderMappings#getJsonUser()} and
 * {@link HeaderMappings#getJsonOrganization()}.
 * </p>
 *
 * @see GeorchestraUsers#resolve
 * @see GeorchestraOrganizations#resolve
 * @see GeorchestraTargetConfig
 */
public class JsonPayloadHeadersContributor extends HeaderContributor {

    /**
     * JSON encoder for serializing {@link GeorchestraUser} and {@link Organization}
     * objects.
     */
    private final ObjectMapper encoder;

    /**
     * Initializes a new {@link JsonPayloadHeadersContributor} with a configured
     * JSON encoder.
     */
    public JsonPayloadHeadersContributor() {
        this.encoder = new ObjectMapper();
        this.encoder.configure(SerializationFeature.INDENT_OUTPUT, false);
        this.encoder.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
        this.encoder.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * Prepares a header contributor that appends JSON-based security headers for
     * the request.
     *
     * @param exchange the current {@link ServerWebExchange}
     * @return a {@link Consumer} that modifies the request headers
     */
    public @Override Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> GeorchestraTargetConfig.getTarget(exchange).map(GeorchestraTargetConfig::headers)
                .ifPresent(mappings -> addJsonPayloads(exchange, mappings, headers));
    }

    private void addJsonPayloads(final ServerWebExchange exchange, final HeaderMappings mappings, HttpHeaders headers) {
        Optional<GeorchestraUser> user = GeorchestraUsers.resolve(exchange);
        Optional<Organization> org = GeorchestraOrganizations.resolve(exchange);

        addJson(headers, "sec-user", mappings.getJsonUser().orElse(false), user);
        addJson(headers, "sec-organization", mappings.getJsonOrganization().orElse(false), org);
    }

    private void addJson(HttpHeaders target, String headerName, boolean enabled, Optional<?> toEncode) {
        if (enabled) {
            toEncode.map(this::encodeJson).map(this::encodeBase64)
                    .ifPresent(encoded -> target.add(headerName, encoded));
        }
    }

    private String encodeJson(Object payloadObject) {
        try {
            return this.encoder.writer().writeValueAsString(payloadObject);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String encodeBase64(String json) {
        return SecurityHeaders.encodeBase64(json);
    }
}
