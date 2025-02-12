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

import java.util.List;

import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.georchestra.gateway.security.ldap.extended.ExtendedLdapAuthenticationConfiguration;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures LDAP-based authentication and authorization for geOrchestra
 * Gateway.
 * <p>
 * This configuration:
 * <ul>
 * <li>Loads LDAP server configurations from
 * {@link GeorchestraGatewaySecurityConfigProperties}.</li>
 * <li>Registers {@link BasicLdapAuthenticationProvider} instances for each
 * enabled LDAP server.</li>
 * <li>Provides a {@link BasicLdapAuthenticatedUserMapper} to convert LDAP
 * authentication data into a geOrchestra user.</li>
 * </ul>
 * </p>
 * <p>
 * Authenticated users will have:
 * <ul>
 * <li>An {@link LdapUserDetails} principal extracted from their LDAP
 * authentication.</li>
 * <li>Roles assigned based on the LDAP group mappings.</li>
 * <li>A security context populated with an {@link Authentication} object.</li>
 * </ul>
 * </p>
 * <p>
 * This configuration primarily supports standard LDAP authentication. For
 * geOrchestra-specific LDAP features (e.g., organizations, additional
 * attributes), refer to {@link ExtendedLdapAuthenticationConfiguration}.
 * </p>
 * 
 * @see ExtendedLdapAuthenticationConfiguration
 * @see GeorchestraGatewaySecurityConfigProperties
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GeorchestraGatewaySecurityConfigProperties.class)
@Slf4j(topic = "org.georchestra.gateway.security.ldap.basic")
public class BasicLdapAuthenticationConfiguration {

    /**
     * Provides an LDAP user mapper for basic authentication configurations.
     *
     * @param enabledConfigs the list of enabled simple LDAP configurations
     * @return a {@link BasicLdapAuthenticatedUserMapper} instance or {@code null}
     *         if no LDAP configurations are enabled
     */
    @Bean
    BasicLdapAuthenticatedUserMapper ldapAuthenticatedUserMapper(List<LdapServerConfig> enabledConfigs) {
        return enabledConfigs.isEmpty() ? null : new BasicLdapAuthenticatedUserMapper();
    }

    /**
     * Retrieves the list of enabled simple (non-extended) LDAP configurations.
     *
     * @param config the security configuration properties
     * @return a list of enabled {@link LdapServerConfig} instances
     */
    @Bean
    List<LdapServerConfig> enabledSimpleLdapConfigs(GeorchestraGatewaySecurityConfigProperties config) {
        return config.simpleEnabled();
    }

    /**
     * Creates a list of LDAP authentication providers based on the enabled LDAP
     * configurations.
     *
     * @param configs the list of enabled LDAP configurations
     * @return a list of {@link BasicLdapAuthenticationProvider} instances
     */
    @Bean
    List<BasicLdapAuthenticationProvider> ldapAuthenticationProviders(List<LdapServerConfig> configs) {
        return configs.stream().map(this::createLdapProvider).toList();
    }

    /**
     * Creates an {@link BasicLdapAuthenticationProvider} for a given LDAP
     * configuration.
     *
     * @param config the LDAP server configuration
     * @return an initialized {@link BasicLdapAuthenticationProvider} instance
     * @throws BeanCreationException if an error occurs during provider creation
     */
    private BasicLdapAuthenticationProvider createLdapProvider(LdapServerConfig config) {
        log.info("Creating LDAP AuthenticationProvider '{}' with URL {}", config.getName(), config.getUrl());

        try {
            LdapAuthenticationProvider provider = new LdapAuthenticatorProviderBuilder().url(config.getUrl())
                    .baseDn(config.getBaseDn()).userSearchBase(config.getUsersRdn())
                    .userSearchFilter(config.getUsersSearchFilter()).rolesSearchBase(config.getRolesRdn())
                    .rolesSearchFilter(config.getRolesSearchFilter()).adminDn(config.getAdminDn().orElse(null))
                    .adminPassword(config.getAdminPassword().orElse(null))
                    .returningAttributes(config.getReturningAttributes()).build();
            return new BasicLdapAuthenticationProvider(config.getName(), provider);
        } catch (RuntimeException e) {
            throw new BeanCreationException("Error creating LDAP Authentication Provider for config " + config.getName()
                    + ": " + e.getMessage(), e);
        }
    }
}
