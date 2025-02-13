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

import org.georchestra.gateway.security.ldap.AuthenticationProviderDecorator;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "org.georchestra.gateway.security.ldap")
public class BasicLdapAuthenticationProvider extends AuthenticationProviderDecorator {

    private final @NonNull String configName;

    public BasicLdapAuthenticationProvider(@NonNull String configName, @NonNull AuthenticationProvider delegate) {
        super(delegate);
        this.configName = configName;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to authenticate user {} against {} LDAP", authentication.getName(), configName);
        try {
            Authentication auth = super.authenticate(authentication);
            log.debug("Authenticated {} from {} with roles {}", auth.getName(), configName, auth.getAuthorities());
            return auth;
        } catch (AuthenticationException e) {
            if (log.isDebugEnabled()) {
                log.info("Authentication of {} against {} LDAP failed", authentication.getName(), configName, e);
            } else {
                log.info("Authentication of {} against {} LDAP failed: {}", authentication.getName(), configName,
                        e.getMessage());
            }
            throw e;
        }
    }

}