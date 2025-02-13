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

import org.georchestra.gateway.security.ldap.AuthenticationProviderDecorator;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * LDAP authentication provider for geOrchestra with extended user management
 * capabilities.
 * <p>
 * This provider wraps an existing {@link AuthenticationProvider} and enhances
 * it with additional behavior specific to geOrchestra. It ensures that
 * authenticated users are associated with the correct LDAP configuration and
 * returns a {@link GeorchestraUserNamePasswordAuthenticationToken} upon
 * successful authentication.
 * </p>
 *
 * <p>
 * <b>Performance Consideration:</b>
 * </p>
 * <p>
 * Under heavy load, the LDAP server may be overwhelmed and start failing
 * authentication requests. To mitigate this, a Caffeine cache with a short TTL
 * could be introduced to handle concurrency and avoid redundant authentication
 * attempts when multiple requests arrive simultaneously.
 * </p>
 */
@Slf4j(topic = "org.georchestra.gateway.security.ldap.extended")
public class GeorchestraLdapAuthenticationProvider extends AuthenticationProviderDecorator {

    private final @NonNull String configName;

    /**
     * Constructs a new {@code GeorchestraLdapAuthenticationProvider} that wraps a
     * delegate authentication provider.
     *
     * @param configName the name of the LDAP configuration associated with this
     *                   provider
     * @param delegate   the actual LDAP authentication provider being decorated
     */
    public GeorchestraLdapAuthenticationProvider(@NonNull String configName, @NonNull AuthenticationProvider delegate) {
        super(delegate);
        this.configName = configName;
    }

    /**
     * Attempts to authenticate a user against the configured LDAP authentication
     * provider.
     * <p>
     * If authentication succeeds, it wraps the authentication result in a
     * {@link GeorchestraUserNamePasswordAuthenticationToken}, ensuring that the
     * user's authentication is correctly associated with the configured LDAP
     * instance.
     * </p>
     *
     * @param authentication the authentication request object
     * @return the authenticated token, or {@code null} if authentication fails
     * @throws AuthenticationException if authentication is unsuccessful
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to authenticate user {} against {} extended LDAP", authentication.getName(), configName);
        try {
            Authentication auth = super.authenticate(authentication);
            log.debug("Authenticated {} from {} with roles {}", auth.getName(), configName, auth.getAuthorities());
            return new GeorchestraUserNamePasswordAuthenticationToken(configName, auth);
        } catch (AuthenticationException e) {
            if (log.isDebugEnabled()) {
                log.info("Authentication of {} against {} extended LDAP failed", authentication.getName(), configName,
                        e);
            } else {
                log.info("Authentication of {} against {} extended LDAP failed: {}", authentication.getName(),
                        configName, e.getMessage());
            }
            throw e;
        }
    }
}
