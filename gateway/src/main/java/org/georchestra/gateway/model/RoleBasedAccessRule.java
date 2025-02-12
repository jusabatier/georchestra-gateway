/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.model;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * Defines access rules for intercepted Ant-pattern URIs based on user roles.
 * <p>
 * Role names correspond to the authenticated user's
 * {@link AbstractAuthenticationToken#getAuthorities() authority names}, which
 * are obtained via {@link GrantedAuthority#getAuthority()}.
 * </p>
 */
@Data
@Generated
@Accessors(chain = true)
public class RoleBasedAccessRule {

    /**
     * List of Ant pattern URIs (excluding the application context) that the gateway
     * intercepts to enforce access rules.
     */
    private List<String> interceptUrl = List.of();

    /**
     * Specifies whether access to the intercepted URLs is explicitly forbidden.
     * <p>
     * If set to {@code true}, access is denied to all users, regardless of role.
     * </p>
     */
    private boolean forbidden = false;

    /**
     * Determines whether anonymous (unauthenticated) access is allowed.
     * <p>
     * If set to {@code true}, no additional role-based access checks are applied to
     * the intercepted URLs, meaning all users can access them. <br>
     * If set to {@code false} and {@link #allowedRoles} is empty, access is granted
     * to any authenticated user.
     * </p>
     */
    private boolean anonymous = false;

    /**
     * Specifies the roles required to access the intercepted URIs.
     * <p>
     * If the list is empty and {@link #anonymous} is {@code false}, any
     * authenticated user is granted access. <br>
     * Role names can be provided with or without the {@code ROLE_} prefix. For
     * example, the role set {@code [ROLE_USER, ROLE_AUDITOR]} is equivalent to
     * {@code [USER, AUDITOR]}.
     * </p>
     */
    private List<String> allowedRoles = List.of();
}
