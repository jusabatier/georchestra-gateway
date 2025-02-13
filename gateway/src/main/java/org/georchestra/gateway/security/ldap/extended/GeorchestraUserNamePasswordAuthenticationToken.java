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

import java.util.Collection;

import org.georchestra.security.api.UsersApi;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A specialized {@link Authentication} object for Georchestra extensions aware
 * LDAP databases, such as the default OpenLDAP schema, where {@link UsersApi}
 * can be used to fetch additional user identity information.
 */
@RequiredArgsConstructor
public class GeorchestraUserNamePasswordAuthenticationToken implements Authentication {

    private static final long serialVersionUID = 1L;

    private final @NonNull @Getter String configName;
    private final @NonNull Authentication orig;

    @Override
    public String getName() {
        return orig.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return orig.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return orig.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return orig.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return orig.isAuthenticated();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        orig.setAuthenticated(isAuthenticated);
    }
}
