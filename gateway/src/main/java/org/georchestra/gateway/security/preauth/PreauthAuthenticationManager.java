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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A {@link ReactiveAuthenticationManager} and
 * {@link ServerAuthenticationConverter} implementation that enables
 * pre-authentication based on HTTP request headers.
 * <p>
 * This authentication mechanism is designed for use with a trusted reverse
 * proxy or an identity provider that injects pre-authenticated user details
 * into HTTP headers. If the {@code sec-georchestra-preauthenticated} header is
 * set to {@code true}, this manager extracts user details and creates a
 * {@link PreAuthenticatedAuthenticationToken}.
 * </p>
 *
 * <h3>Authentication Flow:</h3>
 * <ol>
 * <li>Checks for the presence of the {@code sec-georchestra-preauthenticated}
 * header.</li>
 * <li>If present, extracts user details from pre-authentication headers.</li>
 * <li>Creates a {@link PreAuthenticatedAuthenticationToken} with the extracted
 * details.</li>
 * <li>Returns the token for authentication.</li>
 * </ol>
 *
 * <h3>Expected Headers:</h3> The following request headers are parsed for
 * authentication:
 * <ul>
 * <li>{@code preauth-username} - (Required) Username of the authenticated
 * user.</li>
 * <li>{@code preauth-email} - (Optional) User's email address.</li>
 * <li>{@code preauth-firstname} - (Optional) First name of the user.</li>
 * <li>{@code preauth-lastname} - (Optional) Last name of the user.</li>
 * <li>{@code preauth-org} - (Optional) Organization name.</li>
 * <li>{@code preauth-roles} - (Optional) Comma-separated list of user
 * roles.</li>
 * <li>{@code preauth-provider} - (Optional) External authentication
 * provider.</li>
 * <li>{@code preauth-provider-id} - (Optional) Identifier from the external
 * provider.</li>
 * </ul>
 * <p>
 * <b>Note:</b> If {@code preauth-roles} is not provided, the user is assigned
 * the default role {@code ROLE_USER}.
 * </p>
 */
public class PreauthAuthenticationManager implements ReactiveAuthenticationManager, ServerAuthenticationConverter {

    public static final String PREAUTH_HEADER_NAME = "sec-georchestra-preauthenticated";

    public static final String PREAUTH_USERNAME = "preauth-username";
    public static final String PREAUTH_EMAIL = "preauth-email";
    public static final String PREAUTH_FIRSTNAME = "preauth-firstname";
    public static final String PREAUTH_LASTNAME = "preauth-lastname";
    public static final String PREAUTH_ORG = "preauth-org";
    public static final String PREAUTH_ROLES = "preauth-roles";
    public static final String PREAUTH_PROVIDER = "preauth-provider";
    public static final String PREAUTH_PROVIDER_ID = "preauth-provider-id";

    /**
     * Converts an incoming request into a
     * {@link PreAuthenticatedAuthenticationToken} if the request contains valid
     * pre-authentication headers.
     *
     * @param exchange the {@link ServerWebExchange} representing the request
     * @return a {@link Mono} containing the authentication token, or an empty
     *         {@link Mono} if not pre-authenticated
     */
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        if (isPreAuthenticated(exchange)) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String username = headers.getFirst(PREAUTH_USERNAME);
            if (!StringUtils.hasText(username)) {
                throw new IllegalStateException("Pre-authenticated user headers not provided");
            }
            PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(username,
                    extract(headers), List.of());
            return Mono.just(authentication);
        }
        return Mono.empty();
    }

    /**
     * Extracts all pre-authentication headers from the request and returns them as
     * a map.
     *
     * @param headers the HTTP request headers
     * @return a map containing pre-authentication header values
     */
    private Map<String, String> extract(HttpHeaders headers) {
        return headers.toSingleValueMap().entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().startsWith("preauth-"))
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
    }

    /**
     * Authenticates a previously converted
     * {@link PreAuthenticatedAuthenticationToken}.
     *
     * @param authentication the authentication token
     * @return a {@link Mono} containing the authenticated token
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication);
    }

    /**
     * Checks whether a request is pre-authenticated based on the presence of the
     * {@code sec-georchestra-preauthenticated} header.
     *
     * @param exchange the server web exchange containing the request
     * @return {@code true} if the request is pre-authenticated, otherwise
     *         {@code false}
     */
    public static boolean isPreAuthenticated(ServerWebExchange exchange) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        final String preAuthHeader = requestHeaders.getFirst(PREAUTH_HEADER_NAME);
        return Boolean.parseBoolean(preAuthHeader);
    }

    /**
     * Maps extracted request headers into a {@link GeorchestraUser} object.
     *
     * @param requestHeaders a map of extracted request headers
     * @return a {@link GeorchestraUser} instance populated with the extracted data
     */
    public static GeorchestraUser map(Map<String, String> requestHeaders) {
        String username = SecurityHeaders.decode(requestHeaders.get(PREAUTH_USERNAME));
        String email = SecurityHeaders.decode(requestHeaders.get(PREAUTH_EMAIL));
        String firstName = SecurityHeaders.decode(requestHeaders.get(PREAUTH_FIRSTNAME));
        String lastName = SecurityHeaders.decode(requestHeaders.get(PREAUTH_LASTNAME));
        String org = SecurityHeaders.decode(requestHeaders.get(PREAUTH_ORG));
        String rolesValue = SecurityHeaders.decode(requestHeaders.get(PREAUTH_ROLES));
        String provider = SecurityHeaders.decode(requestHeaders.get(PREAUTH_PROVIDER));
        String providerId = SecurityHeaders.decode(requestHeaders.get(PREAUTH_PROVIDER_ID));

        List<String> roleNames = Optional.ofNullable(rolesValue)
                .map(roles -> Stream
                        .concat(Stream.of("ROLE_USER"), Stream.of(roles.split(";")).filter(StringUtils::hasText))
                        .distinct())
                .orElse(Stream.of("ROLE_USER")).toList();

        GeorchestraUser user = new GeorchestraUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOrganization(org);
        user.setRoles(new ArrayList<>(roleNames)); // mutable list
        user.setOAuth2Provider(provider);
        user.setOAuth2Uid(providerId);
        // TODO: Consider renaming OAuth2-related fields to a more generic
        // "externalProvider"
        return user;
    }

    /**
     * Removes pre-authentication headers from a given set of mutable HTTP headers.
     *
     * @param mutableHeaders the mutable {@link HttpHeaders} object to clean up
     */
    public void removePreauthHeaders(HttpHeaders mutableHeaders) {
        mutableHeaders.remove(PREAUTH_HEADER_NAME);
        mutableHeaders.remove(PREAUTH_USERNAME);
        mutableHeaders.remove(PREAUTH_EMAIL);
        mutableHeaders.remove(PREAUTH_FIRSTNAME);
        mutableHeaders.remove(PREAUTH_LASTNAME);
        mutableHeaders.remove(PREAUTH_ORG);
        mutableHeaders.remove(PREAUTH_ROLES);
        mutableHeaders.remove(PREAUTH_PROVIDER);
        mutableHeaders.remove(PREAUTH_PROVIDER_ID);
    }
}
