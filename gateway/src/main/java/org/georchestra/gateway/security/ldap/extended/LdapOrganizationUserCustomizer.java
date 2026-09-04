/*
 * Copyright (C) 2026 by the geOrchestra PSC
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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.security.ldap.extended;

import java.util.Objects;
import java.util.Optional;

import org.georchestra.gateway.security.GeorchestraUserCustomizerExtension;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enriches an OAuth2 user with the organization of an existing LDAP account.
 * <p>
 * This customizer performs read-only lookups. If no matching LDAP account or no
 * organization is found, the OAuth2-mapped user is returned unchanged.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j(topic = "org.georchestra.gateway.security.ldap.extended")
class LdapOrganizationUserCustomizer implements GeorchestraUserCustomizerExtension {

    private final @NonNull DemultiplexingUsersApi users;

    @Override
    public GeorchestraUser apply(Authentication authentication, GeorchestraUser mappedUser) {
        log.debug("LDAP organization lookup: authenticationType={}, mappedUsername={}",
                authentication == null ? null : authentication.getClass().getName(), mappedUser.getUsername());

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.debug("LDAP organization lookup skipped for user {}: authentication is not OAuth2",
                    mappedUser.getUsername());
            return mappedUser;
        }

        String username = mappedUser.getUsername();
        if (!StringUtils.hasText(username)) {
            log.debug("LDAP organization lookup skipped: mapped username is empty");
            return mappedUser;
        }

        Optional<ExtendedGeorchestraUser> ldapUser = users.findByUsername(username);
        log.debug("LDAP organization lookup for user {}: ldapUserFound={}", username, ldapUser.isPresent());
        Optional<Organization> organization = ldapUser.map(ExtendedGeorchestraUser::getOrg).filter(Objects::nonNull);
        if (organization.isEmpty()) {
            log.debug("LDAP organization lookup for user {}: organizationFound=false", username);
            return mappedUser;
        }

        log.debug("LDAP organization lookup for user {}: organizationFound=true, organization={}", username,
                organization.orElseThrow().getShortName());
        ExtendedGeorchestraUser enrichedUser = new ExtendedGeorchestraUser(mappedUser);
        enrichedUser.setOrg(organization.orElseThrow());
        return enrichedUser;
    }
}
