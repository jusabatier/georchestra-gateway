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
package org.georchestra.gateway.security;

import java.util.Optional;

import org.georchestra.security.model.GeorchestraUser;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;

/**
 * Defines an extension point for mapping authentication tokens to
 * {@link GeorchestraUser} instances.
 * <p>
 * This interface allows different authentication mechanisms (e.g., LDAP,
 * OAuth2, OpenID Connect) to provide their own strategy for extracting user
 * details from an {@link Authentication} token.
 * </p>
 * <p>
 * Implementations of this interface are queried by
 * {@link GeorchestraUserMapper} to determine whether they can handle the given
 * authentication token. If a suitable implementation is found, it returns a
 * non-empty {@link GeorchestraUser}.
 * </p>
 * 
 * <p>
 * Beans of this type are {@link Ordered}, meaning multiple resolvers can be
 * defined with explicit ordering to prioritize certain authentication sources
 * over others.
 * </p>
 * 
 * @see GeorchestraUserMapper
 */
public interface GeorchestraUserMapperExtension extends Ordered {

    /**
     * Attempts to map an {@link Authentication} token to a {@link GeorchestraUser}.
     * <p>
     * If this implementation can extract user details from the provided
     * authentication token, it should return a populated {@link GeorchestraUser}.
     * Otherwise, it should return {@link Optional#empty()} to allow other resolvers
     * to handle the token.
     * </p>
     * 
     * @param authToken the authentication token representing the user's credentials
     * @return an optional {@link GeorchestraUser} if this resolver can handle the
     *         authentication token
     */
    Optional<GeorchestraUser> resolve(Authentication authToken);

    /**
     * Defines the order in which this resolver should be executed relative to other
     * {@link GeorchestraUserMapperExtension} implementations.
     * <p>
     * A lower value indicates higher priority.
     * </p>
     * 
     * @return {@code 0} as the default order. Implementations can override this if
     *         needed.
     */
    default int getOrder() {
        return 0;
    }
}
