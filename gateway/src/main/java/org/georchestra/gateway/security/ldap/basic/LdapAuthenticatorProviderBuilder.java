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

import static java.util.Objects.requireNonNull;

import org.georchestra.ds.users.AccountDao;
import org.georchestra.gateway.security.ldap.NoPasswordLdapUserDetailsMapper;
import org.georchestra.gateway.security.ldap.extended.ExtendedLdapAuthenticationProvider;
import org.georchestra.gateway.security.ldap.extended.ExtendedPasswordPolicyAwareContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Builder for creating an {@link ExtendedLdapAuthenticationProvider} instance
 * with a configurable LDAP authentication setup.
 * <p>
 * This builder allows setting:
 * <ul>
 * <li>LDAP connection properties (URL, base DN, admin credentials)</li>
 * <li>User search configuration (search base, filter, returning
 * attributes)</li>
 * <li>Role resolution configuration (group search base and filter)</li>
 * <li>Integration with an optional {@link AccountDao} for user account
 * management</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {
 *     &#64;code
 *     LdapAuthenticationProvider provider = new LdapAuthenticatorProviderBuilder().url("ldap://example.com")
 *             .baseDn("dc=example,dc=com").userSearchBase("ou=users").userSearchFilter("(uid={0})")
 *             .rolesSearchBase("ou=groups").rolesSearchFilter("(member={0})").build();
 * }
 * </pre>
 */
@Accessors(chain = true, fluent = true)
public class LdapAuthenticatorProviderBuilder {

    private @Setter String url;
    private @Setter String baseDn;

    private @Setter String userSearchBase;
    private @Setter String userSearchFilter;

    private @Setter String rolesSearchBase;
    private @Setter String rolesSearchFilter;

    private @Setter String adminDn;
    private @Setter String adminPassword;

    private @Setter AccountDao accountDao;

    /**
     * Attributes to be retrieved when querying LDAP for user details.
     * <p>
     * A {@code null} value retrieves all attributes, while an empty array retrieves
     * none.
     * </p>
     */
    private @Setter String[] returningAttributes = null;

    /**
     * Builds and returns an {@link ExtendedLdapAuthenticationProvider} based on the
     * configured settings.
     *
     * @return an LDAP authentication provider
     * @throws NullPointerException if required fields are not set
     */
    public ExtendedLdapAuthenticationProvider build() {
        requireNonNull(url, "LDAP URL is not set");
        requireNonNull(baseDn, "Base DN is not set");
        requireNonNull(userSearchBase, "User search base is not set");
        requireNonNull(userSearchFilter, "User search filter is not set");
        requireNonNull(rolesSearchBase, "Roles search base is not set");
        requireNonNull(rolesSearchFilter, "Roles search filter is not set");

        final ExtendedPasswordPolicyAwareContextSource contextSource = createContextSource();
        final BindAuthenticator authenticator = createLdapAuthenticator(contextSource);
        final DefaultLdapAuthoritiesPopulator rolesPopulator = createLdapAuthoritiesPopulator(contextSource);

        ExtendedLdapAuthenticationProvider provider = new ExtendedLdapAuthenticationProvider(authenticator,
                rolesPopulator);
        provider.setAuthoritiesMapper(createAuthoritiesMapper());
        provider.setUserDetailsContextMapper(new NoPasswordLdapUserDetailsMapper());
        provider.setAccountDao(accountDao);

        return provider;
    }

    /**
     * Creates and configures the LDAP authenticator.
     */
    private BindAuthenticator createLdapAuthenticator(BaseLdapPathContextSource contextSource) {
        FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter,
                contextSource);
        search.setReturningAttributes(returningAttributes);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(search);
        authenticator.afterPropertiesSet();
        return authenticator;
    }

    /**
     * Creates and configures the LDAP context source for authentication.
     */
    private ExtendedPasswordPolicyAwareContextSource createContextSource() {
        ExtendedPasswordPolicyAwareContextSource context = new ExtendedPasswordPolicyAwareContextSource(url);
        context.setBase(baseDn);
        if (adminDn != null) {
            context.setUserDn(adminDn);
            context.setPassword(adminPassword);
        }
        context.afterPropertiesSet();
        return context;
    }

    /**
     * Creates a default authority mapper to convert LDAP roles into Spring Security
     * authorities.
     */
    private GrantedAuthoritiesMapper createAuthoritiesMapper() {
        return new SimpleAuthorityMapper();
    }

    /**
     * Creates and configures the LDAP role populator.
     */
    private DefaultLdapAuthoritiesPopulator createLdapAuthoritiesPopulator(BaseLdapPathContextSource contextSource) {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
                rolesSearchBase);
        authoritiesPopulator.setGroupSearchFilter(rolesSearchFilter);
        return authoritiesPopulator;
    }
}
