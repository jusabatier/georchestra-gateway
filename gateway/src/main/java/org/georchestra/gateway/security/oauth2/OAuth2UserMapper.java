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
package org.georchestra.gateway.security.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.georchestra.gateway.security.GeorchestraUserMapperExtension;
import org.georchestra.security.model.GeorchestraUser;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps {@link OAuth2AuthenticationToken} to {@link GeorchestraUser}.
 * <p>
 * This class extracts user information from an OAuth2 authentication token and
 * maps it to a {@link GeorchestraUser}. The mapping process follows these
 * rules:
 * </p>
 * <ul>
 * <li>The {@link OAuth2User principal}'s {@code login} attribute is used with
 * preference over {@link OAuth2AuthenticationToken#getName()} if available, as
 * the latter often contains an external system's numeric identifier.</li>
 * <li>The user's email is extracted from the {@code email} attribute, if
 * present.</li>
 * <li>Roles are derived from the granted authorities but exclude any that start
 * with {@code ROLE_SCOPE_} or {@code SCOPE_}.</li>
 * </ul>
 */
@Slf4j(topic = "org.georchestra.gateway.security.oauth2")
public class OAuth2UserMapper implements GeorchestraUserMapperExtension {

    /**
     * Attempts to resolve an OAuth2 authentication token into a
     * {@link GeorchestraUser}.
     *
     * @param authToken The authentication token to resolve.
     * @return An {@link Optional} containing the mapped user if the token is valid,
     *         or {@link Optional#empty()} if it cannot be mapped.
     */
    @Override
    public Optional<GeorchestraUser> resolve(Authentication authToken) {
        return Optional.ofNullable(authToken).filter(OAuth2AuthenticationToken.class::isInstance)
                .map(OAuth2AuthenticationToken.class::cast).filter(tokenFilter()).flatMap(this::map);
    }

    /**
     * Provides a predicate to filter which OAuth2 tokens should be processed.
     * <p>
     * The default implementation accepts all tokens.
     * </p>
     *
     * @return A {@link Predicate} for filtering authentication tokens.
     */
    protected Predicate<OAuth2AuthenticationToken> tokenFilter() {
        return token -> true;
    }

    /**
     * Maps an {@link OAuth2AuthenticationToken} to a {@link GeorchestraUser}.
     *
     * @param token The OAuth2 authentication token.
     * @return An {@link Optional} containing the mapped {@link GeorchestraUser}.
     */
    protected Optional<GeorchestraUser> map(OAuth2AuthenticationToken token) {
        logger().debug("Mapping {} authentication token from provider {}",
                token.getPrincipal().getClass().getSimpleName(), token.getAuthorizedClientRegistrationId());

        OAuth2User oAuth2User = token.getPrincipal();
        GeorchestraUser user = new GeorchestraUser();
        user.setOAuth2Provider(token.getAuthorizedClientRegistrationId());
        user.setOAuth2Uid(token.getName());

        Map<String, Object> attributes = oAuth2User.getAttributes();
        List<String> roles = resolveRoles(oAuth2User.getAuthorities());
        String userName = token.getName();
        String login = (String) attributes.get("login");

        /*
         * Plain OAuth2 authentication user names are often numeric identifiers. The
         * 'login' attribute typically contains a more meaningful name, so it is used in
         * preference to the username if available.
         */
        apply(user::setUsername, login, userName);
        apply(user::setEmail, (String) attributes.get("email"));
        user.setRoles(new ArrayList<>(roles)); // mutable

        return Optional.of(user);
    }

    /**
     * Resolves roles from granted authorities while excluding OAuth2 scope-related
     * authorities.
     *
     * @param authorities The collection of granted authorities.
     * @return A list of resolved role names.
     */
    protected List<String> resolveRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).filter(scope -> {
            if (scope.startsWith("ROLE_SCOPE_") || scope.startsWith("SCOPE_")) {
                logger().debug("Excluding granted authority {}", scope);
                return false;
            }
            return true;
        }).toList();
    }

    /**
     * Applies the first non-null candidate value to the specified setter function.
     *
     * @param setter     The setter function to apply.
     * @param candidates A varargs list of candidate values.
     */
    protected void apply(Consumer<String> setter, String... candidates) {
        for (String candidateValue : candidates) {
            if (candidateValue != null) {
                setter.accept(candidateValue);
                break;
            }
        }
    }

    /**
     * Provides access to the class logger.
     *
     * @return The logger instance.
     */
    protected Logger logger() {
        return log;
    }
}
