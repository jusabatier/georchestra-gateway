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

import java.util.Map;
import java.util.Optional;

import org.georchestra.gateway.security.GeorchestraUserMapperExtension;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * A {@link GeorchestraUserMapperExtension} implementation that maps a
 * {@link PreAuthenticatedAuthenticationToken} to a {@link GeorchestraUser}.
 * <p>
 * This class extracts user details from the credentials of the authentication
 * token, which are expected to be a {@link Map} containing pre-authenticated
 * user attributes.
 * </p>
 *
 * <h3>Mapping Logic:</h3>
 * <ol>
 * <li>Verifies that the provided authentication token is a
 * {@link PreAuthenticatedAuthenticationToken}.</li>
 * <li>Extracts the credentials from the token, expecting them to be a
 * {@link Map}.</li>
 * <li>Uses {@link PreauthAuthenticationManager#map(Map)} to convert the
 * extracted attributes into a {@link GeorchestraUser}.</li>
 * </ol>
 * <p>
 * If the token does not meet these conditions, an empty {@link Optional} is
 * returned.
 * </p>
 */
public class PreauthenticatedUserMapperExtension implements GeorchestraUserMapperExtension {

    /**
     * Resolves a {@link GeorchestraUser} from a pre-authenticated authentication
     * token.
     *
     * @param authToken the authentication token to resolve
     * @return an {@link Optional} containing the mapped {@link GeorchestraUser}, or
     *         empty if resolution fails
     */
    @Override
    public Optional<GeorchestraUser> resolve(Authentication authToken) {
        return Optional.ofNullable(authToken)//
                .filter(PreAuthenticatedAuthenticationToken.class::isInstance) // Ensure token type
                .map(PreAuthenticatedAuthenticationToken.class::cast)//
                .map(PreAuthenticatedAuthenticationToken::getCredentials) // Extract credentials
                .filter(Map.class::isInstance) // Ensure credentials are a Map
                .map(Map.class::cast)//
                .map(PreauthAuthenticationManager::map); // Convert to GeorchestraUser
    }
}
