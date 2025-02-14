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

import static org.georchestra.commons.security.SecurityHeaders.SEC_ADDRESS;
import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_EXTERNAL_AUTHENTICATION;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTUPDATED;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LDAP_REMAINING_DAYS;
import static org.georchestra.commons.security.SecurityHeaders.SEC_NOTES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_TEL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_TITLE;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERID;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.georchestra.gateway.filter.headers.HeaderContributor;
import org.georchestra.gateway.model.GeorchestraTargetConfig;
import org.georchestra.gateway.model.GeorchestraUsers;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * {@link HeaderContributor} that appends user-related {@literal sec-*} security
 * headers to proxied requests.
 * <p>
 * This contributor extracts user information from the current request context
 * and applies the configured security headers based on
 * {@link GeorchestraTargetConfig}.
 * </p>
 *
 * <h3>Appended Headers</h3>
 * <ul>
 * <li>{@code sec-userid} - User ID</li>
 * <li>{@code sec-username} - Username</li>
 * <li>{@code sec-org} - Organization</li>
 * <li>{@code sec-email} - Email address</li>
 * <li>{@code sec-firstname} - First name</li>
 * <li>{@code sec-lastname} - Last name</li>
 * <li>{@code sec-tel} - Telephone number</li>
 * <li>{@code sec-roles} - List of user roles</li>
 * <li>{@code sec-lastupdated} - Last updated timestamp</li>
 * <li>{@code sec-address} - Postal address</li>
 * <li>{@code sec-title} - User title</li>
 * <li>{@code sec-notes} - Notes</li>
 * <li>{@code sec-ldap-remaining-days} - LDAP password expiration warning</li>
 * <li>{@code sec-external-authentication} - Whether the user is authenticated
 * externally</li>
 * </ul>
 */
public class GeorchestraUserHeadersContributor extends HeaderContributor {

    /**
     * Prepares a header contributor that appends user-related security headers to
     * the request.
     * <p>
     * Headers are only added if the user is resolved from the request and the
     * corresponding configuration enables them.
     * </p>
     *
     * @param exchange the current {@link ServerWebExchange}
     * @return a {@link Consumer} that modifies the request headers
     */
    public @Override Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> GeorchestraTargetConfig.getTarget(exchange).map(GeorchestraTargetConfig::headers)
                .ifPresent(mappings -> {
                    Optional<GeorchestraUser> user = GeorchestraUsers.resolve(exchange);
                    add(headers, SEC_USERID, mappings.getUserid(), user.map(GeorchestraUser::getId));
                    add(headers, SEC_USERNAME, mappings.getUsername(), user.map(GeorchestraUser::getUsername));
                    add(headers, SEC_ORG, mappings.getOrg(), user.map(GeorchestraUser::getOrganization));
                    add(headers, SEC_EMAIL, mappings.getEmail(), user.map(GeorchestraUser::getEmail));
                    add(headers, SEC_FIRSTNAME, mappings.getFirstname(), user.map(GeorchestraUser::getFirstName));
                    add(headers, SEC_LASTNAME, mappings.getLastname(), user.map(GeorchestraUser::getLastName));
                    add(headers, SEC_TEL, mappings.getTel(), user.map(GeorchestraUser::getTelephoneNumber));

                    List<String> roles = user.map(GeorchestraUser::getRoles).orElse(List.of());
                    add(headers, SEC_ROLES, mappings.getRoles(), roles);

                    add(headers, SEC_LASTUPDATED, mappings.getLastUpdated(), user.map(GeorchestraUser::getLastUpdated));
                    add(headers, SEC_ADDRESS, mappings.getAddress(), user.map(GeorchestraUser::getPostalAddress));
                    add(headers, SEC_TITLE, mappings.getTitle(), user.map(GeorchestraUser::getTitle));
                    add(headers, SEC_NOTES, mappings.getNotes(), user.map(GeorchestraUser::getNotes));

                    add(headers, SEC_LDAP_REMAINING_DAYS,
                            Optional.of(user.isPresent() && Boolean.TRUE.equals(user.get().getLdapWarn())),
                            user.map(GeorchestraUser::getLdapRemainingDays));

                    add(headers, SEC_EXTERNAL_AUTHENTICATION, Optional.of(user.isPresent()),
                            String.valueOf(user.isPresent() && Boolean.TRUE.equals(user.get().getIsExternalAuth())));
                });
    }
}
