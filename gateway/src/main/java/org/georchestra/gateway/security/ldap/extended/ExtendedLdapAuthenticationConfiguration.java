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

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.roles.RoleDaoImpl;
import org.georchestra.ds.roles.RoleProtected;
import org.georchestra.ds.security.OrganizationMapperImpl;
import org.georchestra.ds.security.OrganizationsApiImpl;
import org.georchestra.ds.security.UserMapper;
import org.georchestra.ds.security.UserMapperImpl;
import org.georchestra.ds.security.UsersApiImpl;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.UserRule;
import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties.Server;
import org.georchestra.gateway.security.GeorchestraUserMapperExtension;
import org.georchestra.gateway.security.ldap.basic.LdapAuthenticatorProviderBuilder;
import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures authentication against an extended LDAP directory, supporting
 * geOrchestra-specific attributes such as organizations and roles.
 * <p>
 * This configuration provides a {@link GeorchestraUserMapperExtension} to
 * transform an authenticated {@link LdapUserDetails} into a
 * {@link GeorchestraUser}, leveraging geOrchestra's LDAP-based user management
 * APIs.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GeorchestraGatewaySecurityConfigProperties.class)
@Slf4j(topic = "org.georchestra.gateway.security.ldap.extended")
public class ExtendedLdapAuthenticationConfiguration {

    /**
     * Registers a user mapper that resolves LDAP-authenticated users to
     * {@link GeorchestraUser}.
     *
     * @param users The {@link DemultiplexingUsersApi} used to look up users in
     *              different LDAP directories.
     * @return A {@link GeorchestraLdapAuthenticatedUserMapper} instance if LDAP
     *         authentication is enabled; otherwise, returns {@code null}.
     */
    @Bean
    GeorchestraLdapAuthenticatedUserMapper georchestraLdapAuthenticatedUserMapper(DemultiplexingUsersApi users) {
        return users.getTargetNames().isEmpty() ? null : new GeorchestraLdapAuthenticatedUserMapper(users);
    }

    /**
     * Retrieves the list of enabled extended LDAP configurations.
     *
     * @param config The global security configuration properties.
     * @return A list of enabled extended LDAP configurations.
     */
    @Bean
    List<ExtendedLdapConfig> enabledExtendedLdapConfigs(GeorchestraGatewaySecurityConfigProperties config) {
        return config.extendedEnabled();
    }

    /**
     * Creates authentication providers for each enabled extended LDAP
     * configuration.
     *
     * @param configs A list of enabled extended LDAP configurations.
     * @return A list of configured {@link GeorchestraLdapAuthenticationProvider}
     *         instances.
     */
    @Bean
    List<GeorchestraLdapAuthenticationProvider> extendedLdapAuthenticationProviders(List<ExtendedLdapConfig> configs) {
        return configs.stream().map(this::createLdapProvider).toList();
    }

    /**
     * Creates a {@link GeorchestraLdapAuthenticationProvider} for the given
     * {@link ExtendedLdapConfig} by setting up the necessary LDAP authentication
     * and authorization mechanisms.
     * <p>
     * This method initializes an {@link LdapTemplate} and an {@link AccountDao}
     * based on the given LDAP configuration. It then builds an
     * {@link ExtendedLdapAuthenticationProvider} using an
     * {@link LdapAuthenticatorProviderBuilder}, setting up the authentication
     * provider with user and role search filters, as well as optional admin
     * credentials if provided.
     * </p>
     *
     * @param config The {@link ExtendedLdapConfig} defining the LDAP connection
     *               details and search configurations.
     * @return A configured {@link GeorchestraLdapAuthenticationProvider} for
     *         handling authentication against the specified LDAP server.
     * @throws IllegalStateException if an error occurs while creating the LDAP
     *                               authentication provider.
     */
    private GeorchestraLdapAuthenticationProvider createLdapProvider(ExtendedLdapConfig config) {
        log.info("Creating extended LDAP AuthenticationProvider {} at {}", config.getName(), config.getUrl());

        final LdapTemplate ldapTemplate;
        try {
            ldapTemplate = ldapTemplate(config);
            final AccountDao accountsDao = accountsDao(ldapTemplate, config);
            ExtendedLdapAuthenticationProvider delegate = new LdapAuthenticatorProviderBuilder()//
                    .url(config.getUrl())//
                    .baseDn(config.getBaseDn())//
                    .userSearchBase(config.getUsersRdn())//
                    .userSearchFilter(config.getUsersSearchFilter())//
                    .rolesSearchBase(config.getRolesRdn())//
                    .rolesSearchFilter(config.getRolesSearchFilter())//
                    .adminDn(config.getAdminDn().orElse(null))//
                    .adminPassword(config.getAdminPassword().orElse(null))//
                    .returningAttributes(config.getReturningAttributes()).accountDao(accountsDao).build();
            return new GeorchestraLdapAuthenticationProvider(config.getName(), delegate);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Registers a {@link DemultiplexingUsersApi} that routes user API calls to the
     * appropriate LDAP instance based on configuration.
     *
     * @param configs The list of extended LDAP configurations.
     * @return A {@link DemultiplexingUsersApi} instance.
     */
    @Bean
    DemultiplexingUsersApi demultiplexingUsersApi(List<ExtendedLdapConfig> configs) {
        Map<String, UsersApi> usersByConfigName = new HashMap<>();
        Map<String, OrganizationsApi> orgsByConfigName = new HashMap<>();
        for (ExtendedLdapConfig config : configs) {
            try {
                LdapTemplate ldapTemplate = ldapTemplate(config);
                AccountDao accountsDao = accountsDao(ldapTemplate, config);
                UsersApi usersApi = createUsersApi(config, ldapTemplate, accountsDao);
                OrganizationsApi orgsApi = createOrgsApi(config, ldapTemplate, accountsDao);
                usersByConfigName.put(config.getName(), usersApi);
                orgsByConfigName.put(config.getName(), orgsApi);
            } catch (Exception ex) {
                throw new BeanInitializationException(
                        "Error creating georchestra users api for ldap config " + config.getName(), ex);
            }
        }
        return new DemultiplexingUsersApi(usersByConfigName, orgsByConfigName);
    }

    //////////////////////////////////////////////
    /// Low level LDAP account management beans
    //////////////////////////////////////////////

    private OrganizationsApi createOrgsApi(ExtendedLdapConfig ldapConfig, LdapTemplate ldapTemplate,
            AccountDao accountsDao) {
        OrganizationsApiImpl impl = new OrganizationsApiImpl();
        OrgsDaoImpl orgsDao = new OrgsDaoImpl();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setAccountDao(accountsDao);
        orgsDao.setBasePath(ldapConfig.getBaseDn());
        orgsDao.setOrgSearchBaseDN(ldapConfig.getOrgsRdn());
        orgsDao.setPendingOrgSearchBaseDN(ldapConfig.getPendingOrgsRdn());
        impl.setOrgsDao(orgsDao);
        impl.setOrgMapper(new OrganizationMapperImpl());
        return impl;
    }

    private UsersApi createUsersApi(ExtendedLdapConfig ldapConfig, LdapTemplate ldapTemplate, AccountDao accountsDao) {
        final RoleDao roleDao = roleDao(ldapTemplate, ldapConfig, accountsDao);

        final UserMapper ldapUserMapper = createUserMapper(roleDao);
        UserRule userRule = ldapUserRule();

        UsersApiImpl impl = new UsersApiImpl();
        impl.setAccountsDao(accountsDao);
        impl.setMapper(ldapUserMapper);
        impl.setUserRule(userRule);
        return impl;
    }

    private UserMapper createUserMapper(RoleDao roleDao) {
        UserMapperImpl impl = new UserMapperImpl();
        impl.setRoleDao(roleDao);
        return impl;
    }

    private LdapTemplate ldapTemplate(ExtendedLdapConfig server) throws Exception {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(server.getUrl());
        contextSource.setBase(server.getBaseDn());
        contextSource.afterPropertiesSet();

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.afterPropertiesSet();
        return ldapTemplate;
    }

    private AccountDao accountsDao(LdapTemplate ldapTemplate, ExtendedLdapConfig ldapConfig) {
        String baseDn = ldapConfig.getBaseDn();
        String userSearchBaseDN = ldapConfig.getUsersRdn();
        String roleSearchBaseDN = ldapConfig.getRolesRdn();

        // we don't need a configuration property for this,
        // we don't allow pending users to log in. The LdapAuthenticationProvider won't
        // even look them up.
        final String pendingUsersSearchBaseDN = "ou=pendingusers";

        AccountDaoImpl impl = new AccountDaoImpl(ldapTemplate);
        impl.setBasePath(baseDn);
        impl.setUserSearchBaseDN(userSearchBaseDN);
        impl.setRoleSearchBaseDN(roleSearchBaseDN);
        impl.setPendingUserSearchBaseDN(pendingUsersSearchBaseDN);

        String orgSearchBaseDN = ldapConfig.getOrgsRdn();
        requireNonNull(orgSearchBaseDN);
        impl.setOrgSearchBaseDN(orgSearchBaseDN);

        // not needed here, only console cares, we shouldn't allow to authenticate
        // pending users, should we?
        final String pendingOrgSearchBaseDN = "ou=pendingorgs";
        impl.setPendingOrgSearchBaseDN(pendingOrgSearchBaseDN);

        impl.init();
        return impl;
    }

    private RoleDao roleDao(LdapTemplate ldapTemplate, ExtendedLdapConfig ldapConfig, AccountDao accountDao) {
        final String rolesRdn = ldapConfig.getRolesRdn();
        RoleDaoImpl impl = new RoleDaoImpl();
        impl.setLdapTemplate(ldapTemplate);
        impl.setRoleSearchBaseDN(rolesRdn);
        impl.setAccountDao(accountDao);
        impl.setRoles(ldapProtectedRoles());
        return impl;
    }

    @SuppressWarnings("unused")
    private OrgsDao orgsDao(LdapTemplate ldapTemplate, Server ldapConfig) {
        OrgsDaoImpl impl = new OrgsDaoImpl();
        impl.setLdapTemplate(ldapTemplate);
        impl.setBasePath(ldapConfig.getBaseDn());
        impl.setOrgSearchBaseDN(ldapConfig.getOrgs().getRdn());

        final String pendingOrgSearchBaseDN = "ou=pendingorgs";
        impl.setPendingOrgSearchBaseDN(pendingOrgSearchBaseDN);
        return impl;
    }

    private UserRule ldapUserRule() {
        // we can't possibly try to delete a protected user, so no need to configure
        // them
        List<String> protectedUsers = Collections.emptyList();
        UserRule rule = new UserRule();
        rule.setListOfprotectedUsers(protectedUsers.toArray(String[]::new));
        return rule;
    }

    private RoleProtected ldapProtectedRoles() {
        // protected roles are used by the console service to avoid deleting them. This
        // application will never try to do so, so we don't care about configuring them
        List<String> protectedRoles = List.of();
        RoleProtected bean = new RoleProtected();
        bean.setListOfprotectedRoles(protectedRoles.toArray(String[]::new));
        return bean;
    }

}
