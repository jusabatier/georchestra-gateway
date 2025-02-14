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
package org.georchestra.gateway.security.ldap.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.georchestra.gateway.security.GeorchestraUserMapperExtension;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Maps LDAP-authenticated tokens to {@link GeorchestraUser} instances by
 * retrieving user details from the configured {@link UsersApi}.
 * <p>
 * This implementation specifically handles instances of
 * {@link GeorchestraUserNamePasswordAuthenticationToken}, using its
 * {@link GeorchestraUserNamePasswordAuthenticationToken#getConfigName()
 * configuration name} to resolve users from the correct LDAP database.
 * </p>
 * <p>
 * Additionally, this class ensures role name consistency by normalizing
 * mismatched prefixes between LDAP authorities and geOrchestra roles.
 * </p>
 *
 * @see DemultiplexingUsersApi
 */
@RequiredArgsConstructor
class GeorchestraLdapAuthenticatedUserMapper implements GeorchestraUserMapperExtension {

    private final @NonNull DemultiplexingUsersApi users;

    @Override
    public Optional<GeorchestraUser> resolve(Authentication authToken) {
        return Optional.ofNullable(authToken).filter(GeorchestraUserNamePasswordAuthenticationToken.class::isInstance)
                .map(GeorchestraUserNamePasswordAuthenticationToken.class::cast)
                .filter(token -> token.getPrincipal() instanceof LdapUserDetails).flatMap(this::map);
    }

    /**
     * Retrieves user details from the appropriate LDAP database based on the
     * authentication token's configuration name.
     *
     * @param token the LDAP authentication token
     * @return an {@link Optional} containing the resolved {@link GeorchestraUser},
     *         or empty if no user was found
     */
    Optional<GeorchestraUser> map(GeorchestraUserNamePasswordAuthenticationToken token) {
        final LdapUserDetails principal = (LdapUserDetails) token.getPrincipal();
        final String ldapConfigName = token.getConfigName();
        final String username = principal.getUsername();

        Optional<ExtendedGeorchestraUser> user = users.findByUsername(ldapConfigName, username);
        return user.map(u -> fixPrefixedRoleNames(u, token));
    }

    /**
     * Ensures that role names are properly prefixed with "ROLE_" for consistency
     * between LDAP and geOrchestra role management.
     * <p>
     * Also updates LDAP password expiration details in the user object.
     * </p>
     *
     * @param user  the resolved user object
     * @param token the authentication token containing authorities
     * @return the updated {@link GeorchestraUser} with normalized roles
     */
    private GeorchestraUser fixPrefixedRoleNames(GeorchestraUser user,
            GeorchestraUserNamePasswordAuthenticationToken token) {

        final LdapUserDetailsImpl principal = (LdapUserDetailsImpl) token.getPrincipal();

        // Ensure consistent role naming by normalizing both authorities and user roles
        Stream<String> authorityRoleNames = token.getAuthorities().stream()
                .filter(SimpleGrantedAuthority.class::isInstance).map(GrantedAuthority::getAuthority)
                .map(this::normalize);

        Stream<String> userRoles = user.getRoles().stream().map(this::normalize);

        List<String> roles = Stream.concat(authorityRoleNames, userRoles).distinct().toList();
        user.setRoles(new ArrayList<>(roles));

        // Set LDAP password expiration warnings if applicable
        if (principal.getTimeBeforeExpiration() < Integer.MAX_VALUE) {
            user.setLdapWarn(true);
            user.setLdapRemainingDays(String.valueOf(principal.getTimeBeforeExpiration() / (60 * 60 * 24)));
        } else {
            user.setLdapWarn(false);
        }

        return user;
    }

    /**
     * Normalizes role names by ensuring they start with "ROLE_".
     *
     * @param role the original role name
     * @return the normalized role name
     */
    private String normalize(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
