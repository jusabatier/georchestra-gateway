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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
 * {@link GeorchestraUserMapperExtension} that maps generic LDAP-authenticated
 * token to {@link GeorchestraUser} by calling
 * {@link UsersApi#findByUsername(String)}, with the authentication token's
 * principal name as argument.
 */
@RequiredArgsConstructor
public class BasicLdapAuthenticatedUserMapper implements GeorchestraUserMapperExtension {

    @Override
    public Optional<GeorchestraUser> resolve(Authentication authToken) {
        return Optional.ofNullable(authToken)//
                .filter(UsernamePasswordAuthenticationToken.class::isInstance)
                .map(UsernamePasswordAuthenticationToken.class::cast)//
                .filter(token -> token.getPrincipal() instanceof LdapUserDetails)//
                .flatMap(this::map);
    }

    Optional<GeorchestraUser> map(UsernamePasswordAuthenticationToken token) {
        final LdapUserDetails principal = (LdapUserDetails) token.getPrincipal();
        final String username = principal.getUsername();
        List<String> roles = resolveRoles(token.getAuthorities());

        GeorchestraUser user = new GeorchestraUser();
        user.setUsername(username);
        user.setRoles(roles);

        if (principal instanceof Person) {
            Person person = (Person) principal;
            String description = person.getDescription();
            String givenName = person.getGivenName();
            String telephoneNumber = person.getTelephoneNumber();
            user.setNotes(description);
            user.setFirstName(givenName);
            user.setTelephoneNumber(telephoneNumber);
        }
        return Optional.of(user);
    }

    protected List<String> resolveRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}
