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

import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

/**
 * Custom implementation of {@link LdapUserDetailsMapper} that prevents storing
 * passwords in the security context.
 * <p>
 * This class overrides the default password mapping behavior to always return
 * {@code null}, ensuring that passwords retrieved from LDAP are not retained in
 * memory.
 * </p>
 */
public class NoPasswordLdapUserDetailsMapper extends LdapUserDetailsMapper {

    /**
     * Overrides the default password mapping to always return {@code null},
     * ensuring that the user's password is never stored in the security context.
     *
     * @param passwordValue the original password value from LDAP
     * @return always {@code null}
     */
    @Override
    protected String mapPassword(Object passwordValue) {
        return null;
    }
}
