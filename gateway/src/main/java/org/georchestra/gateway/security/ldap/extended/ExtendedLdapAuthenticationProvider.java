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

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Custom LDAP authentication provider that extends
 * {@link LdapAuthenticationProvider} to support user lookups by email and
 * additional account resolution logic.
 * <p>
 * This provider first attempts to resolve the authenticated user's email to a
 * corresponding LDAP account via {@link AccountDao}. If a match is found,
 * authentication proceeds using the associated UID instead of the provided
 * email.
 * <p>
 * This approach ensures that users can log in with their email addresses while
 * maintaining compatibility with LDAP-based user identification.
 */
public class ExtendedLdapAuthenticationProvider extends LdapAuthenticationProvider {

    private AccountDao accountDao;

    /**
     * Constructs an {@link ExtendedLdapAuthenticationProvider} using the specified
     * {@link LdapAuthenticator} and {@link LdapAuthoritiesPopulator}.
     *
     * @param authenticator        the {@link LdapAuthenticator} used for
     *                             authentication
     * @param authoritiesPopulator the {@link LdapAuthoritiesPopulator} used to load
     *                             user authorities
     */
    public ExtendedLdapAuthenticationProvider(LdapAuthenticator authenticator,
            LdapAuthoritiesPopulator authoritiesPopulator) {
        super(authenticator, authoritiesPopulator);
    }

    /**
     * Sets the {@link AccountDao} used to resolve accounts by email.
     *
     * @param accountDao the {@link AccountDao} instance
     */
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Authenticates a user by first attempting to resolve the account via email
     * lookup, then delegating to the parent class for authentication against LDAP.
     *
     * @param authentication the authentication request object
     * @return an authenticated {@link Authentication} instance if successful
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("LdapAuthenticationProvider.onlySupports",
                        "Only UsernamePasswordAuthenticationToken is supported"));

        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
        Account account = null;

        try {
            account = accountDao.findByEmail(userToken.getName());
        } catch (DataServiceException | NameNotFoundException ignored) {
            // Swallow exceptions and proceed with normal authentication if account is not
            // found
        }

        // If an account was found, replace the authentication token with its UID
        if (account != null) {
            userToken = new UsernamePasswordAuthenticationToken(account.getUid(), userToken.getCredentials());
        }

        String username = userToken.getName();
        String password = (String) authentication.getCredentials();

        if (!StringUtils.hasLength(username)) {
            throw new BadCredentialsException(
                    this.messages.getMessage("LdapAuthenticationProvider.emptyUsername", "Empty Username"));
        }
        if (!StringUtils.hasLength(password)) {
            throw new BadCredentialsException(
                    this.messages.getMessage("AbstractLdapAuthenticationProvider.emptyPassword", "Empty Password"));
        }

        Assert.notNull(password, "Null password was supplied in authentication token");

        // Perform LDAP authentication
        DirContextOperations userData = doAuthentication(userToken);
        UserDetails user = this.userDetailsContextMapper.mapUserFromContext(userData, username,
                loadUserAuthorities(userData, authentication.getName(), (String) authentication.getCredentials()));

        return createSuccessfulAuthentication(userToken, user);
    }
}
