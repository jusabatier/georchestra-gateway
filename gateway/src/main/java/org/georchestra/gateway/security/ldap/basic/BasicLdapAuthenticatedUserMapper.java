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

package org.georchestra.gateway.security.ldap.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.georchestra.gateway.security.GeorchestraUserMapperExtension;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.Person;

import lombok.RequiredArgsConstructor;

/**
 * Maps a generic LDAP-authenticated {@link Authentication} token to a
 * {@link GeorchestraUser}.
 * <p>
 * This implementation extracts user details from an
 * {@link LdapUserDetails}-based authentication and maps them to a
 * {@link GeorchestraUser}. It retrieves:
 * <ul>
 * <li>Username from {@link LdapUserDetails#getUsername()}</li>
 * <li>Roles from {@link Authentication#getAuthorities()}</li>
 * <li>Additional attributes (first name, telephone, description) if available
 * from a {@link Person} instance.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This mapper does <b>not</b> interact with {@link UsersApi}, unlike other
 * implementations.
 * </p>
 */
@RequiredArgsConstructor
public class BasicLdapAuthenticatedUserMapper implements GeorchestraUserMapperExtension {

    /**
     * Attempts to resolve a {@link GeorchestraUser} from the provided
     * authentication token.
     *
     * @param authToken the authentication token to process
     * @return an {@link Optional} containing the mapped {@link GeorchestraUser}, or
     *         empty if the token does not match the expected type
     */
    @Override
    public Optional<GeorchestraUser> resolve(Authentication authToken) {
        return Optional.ofNullable(authToken).filter(UsernamePasswordAuthenticationToken.class::isInstance)
                .map(UsernamePasswordAuthenticationToken.class::cast)
                .filter(token -> token.getPrincipal() instanceof LdapUserDetails).flatMap(this::map);
    }

    /**
     * Maps an LDAP-authenticated user to a {@link GeorchestraUser}.
     *
     * @param token the authentication token containing LDAP user details
     * @return an {@link Optional} containing the mapped {@link GeorchestraUser}
     */
    Optional<GeorchestraUser> map(UsernamePasswordAuthenticationToken token) {
        LdapUserDetails principal = (LdapUserDetails) token.getPrincipal();
        String username = principal.getUsername();
        List<String> roles = resolveRoles(token.getAuthorities());

        GeorchestraUser user = new GeorchestraUser();
        user.setUsername(username);
        user.setRoles(new ArrayList<>(roles)); // Ensure roles are mutable

        if (principal instanceof Person person) {
            user.setFirstName(person.getGivenName());
            user.setTelephoneNumber(person.getTelephoneNumber());
            user.setNotes(person.getDescription());
        }
        return Optional.of(user);
    }

    /**
     * Extracts role names from the authentication token's authorities.
     *
     * @param authorities the granted authorities assigned to the user
     * @return a list of role names
     */
    protected List<String> resolveRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
