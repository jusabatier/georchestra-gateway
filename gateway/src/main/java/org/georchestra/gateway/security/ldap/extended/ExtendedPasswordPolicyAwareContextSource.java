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

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.core.log.LogMessage;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.ldap.ppolicy.PasswordPolicyAwareContextSource;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControlExtractor;
import org.springframework.security.ldap.ppolicy.PasswordPolicyException;
import org.springframework.security.ldap.ppolicy.PasswordPolicyResponseControl;

/**
 * Extended version of {@link PasswordPolicyAwareContextSource} that enforces
 * LDAP password policy controls when binding as a user.
 * <p>
 * This implementation first binds as the manager user before attempting to
 * rebind as the actual principal, allowing for password policy controls to be
 * applied correctly.
 * <p>
 * If the password policy control response indicates an error (e.g., password
 * expired, account locked), a {@link PasswordPolicyException} is thrown.
 */
public class ExtendedPasswordPolicyAwareContextSource extends PasswordPolicyAwareContextSource {

    /**
     * Constructs an {@code ExtendedPasswordPolicyAwareContextSource} with the given
     * LDAP provider URL.
     *
     * @param providerUrl the LDAP provider URL
     */
    public ExtendedPasswordPolicyAwareContextSource(String providerUrl) {
        super(providerUrl);
    }

    /**
     * Obtains an LDAP {@link DirContext} for the specified principal and
     * credentials, enforcing LDAP password policy controls.
     * <p>
     * If binding as the configured manager user (admin DN), it delegates directly
     * to {@link PasswordPolicyAwareContextSource#getContext(String, String)}.
     * Otherwise, it first binds as the manager user before reconnecting as the
     * specified principal to ensure password policy controls are properly applied.
     *
     * @param principal   the distinguished name (DN) of the user to authenticate
     * @param credentials the user's credentials
     * @return a bound {@link DirContext} for the authenticated user
     * @throws PasswordPolicyException if the LDAP server enforces password policy
     *                                 restrictions
     */
    @Override
    public DirContext getContext(String principal, String credentials) throws PasswordPolicyException {
        final String userdn = getUserDn();
        if (principal.equals(userdn)) {
            return super.getContext(principal, credentials);
        }

        this.logger.trace(LogMessage.format("Binding as %s, prior to reconnect as user %s", userdn, principal));

        // Bind as the manager user before re-binding as the specific principal
        LdapContext ctx = (LdapContext) super.getContext(userdn, getPassword());
        Control[] rctls = { new PasswordPolicyControl(false) };

        try {
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, principal);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, credentials);
            ctx.reconnect(rctls);
        } catch (javax.naming.NamingException ex) {
            PasswordPolicyResponseControl ctrl = PasswordPolicyControlExtractor.extractControl(ctx);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Failed to bind with %s", ctrl), ex);
            }

            LdapUtils.closeContext(ctx);

            if (ctrl != null && ctrl.getErrorStatus() != null) {
                throw new PasswordPolicyException(ctrl.getErrorStatus());
            }

            throw LdapUtils.convertLdapException(ex);
        }

        this.logger.debug(LogMessage.of(() -> "Bound with " + PasswordPolicyControlExtractor.extractControl(ctx)));
        return ctx;
    }
}
