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

package org.georchestra.gateway.security.ldap;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract decorator for {@link AuthenticationProvider} implementations.
 * <p>
 * This class delegates authentication and support checks to another
 * {@link AuthenticationProvider}, allowing additional processing in subclasses
 * without modifying the original authentication logic.
 * </p>
 *
 * @see AuthenticationProvider
 */
@RequiredArgsConstructor
public abstract class AuthenticationProviderDecorator implements AuthenticationProvider {

    private final @NonNull AuthenticationProvider delegate;

    /**
     * Determines whether this {@link AuthenticationProvider} supports the specified
     * authentication class.
     *
     * @param authentication the authentication class to check
     * @return {@code true} if the provider supports the given authentication type,
     *         otherwise {@code false}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }

    /**
     * Authenticates the given {@link Authentication} request.
     * <p>
     * This method delegates authentication to the underlying
     * {@link AuthenticationProvider}.
     * </p>
     *
     * @param authentication the authentication request
     * @return a fully authenticated object, or {@code null} if authentication was
     *         unsuccessful
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return delegate.authenticate(authentication);
    }
}
