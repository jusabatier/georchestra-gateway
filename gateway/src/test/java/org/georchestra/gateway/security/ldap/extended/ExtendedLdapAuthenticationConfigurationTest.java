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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

/**
 * Tests {@link ApplicationContext} contributions of
 * {@link ExtendedLdapAuthenticationConfiguration}
 */
class ExtendedLdapAuthenticationConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(UserConfigurations.of(ExtendedLdapAuthenticationConfiguration.class));

    @SuppressWarnings("unchecked")
    @Test
    void contextContributions_empty_config() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).getBean("enabledExtendedLdapConfigs").isInstanceOf(List.class);
            assertThat(context.getBean("enabledExtendedLdapConfigs", List.class)).isEmpty();

            assertThat(context.getBean("georchestraLdapAuthenticatedUserMapper").getClass().getName())
                    .isEqualTo("org.springframework.beans.factory.support.NullBean");

            assertThat(context.getBean(DemultiplexingUsersApi.class)).hasFieldOrPropertyWithValue("targetNames",
                    Set.of());
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void contextContributions_single_config() {
        runner.withPropertyValues(""//
                , "georchestra.gateway.security.ldap.ldap1.enabled: true" //
                , "georchestra.gateway.security.ldap.ldap1.extended: true" //
                , "georchestra.gateway.security.ldap.ldap1.url: ldap://ldap1.test.com:839" //
                , "georchestra.gateway.security.ldap.ldap1.baseDn: dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap1.users.rdn: ou=users,dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap1.users.searchFilter: (uid={0})" //
                , "georchestra.gateway.security.ldap.ldap1.roles.rdn: ou=roles" //
                , "georchestra.gateway.security.ldap.ldap1.roles.searchFilter: (member={0})" //
                , "georchestra.gateway.security.ldap.ldap1.orgs.rdn: ou=orgs" //
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).getBean("enabledExtendedLdapConfigs").isInstanceOf(List.class);
            assertThat(context.getBean("enabledExtendedLdapConfigs", List.class)).hasSize(1);
            assertThat(context.getBean("extendedLdapAuthenticationProviders", List.class)).hasSize(1);
            assertThat(context.getBean("extendedLdapAuthenticationProviders", List.class)).singleElement()
                    .isInstanceOf(GeorchestraLdapAuthenticationProvider.class);

            assertThat(context.getBean("georchestraLdapAuthenticatedUserMapper"))
                    .isInstanceOf(GeorchestraLdapAuthenticatedUserMapper.class);

            assertThat(context.getBean(DemultiplexingUsersApi.class)).hasFieldOrPropertyWithValue("targetNames",
                    Set.of("ldap1"));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void contextContributions_multiple_configs() {
        runner.withPropertyValues(""//
                , "georchestra.gateway.security.ldap.ldap1.enabled: true" //
                , "georchestra.gateway.security.ldap.ldap1.extended: true" //
                , "georchestra.gateway.security.ldap.ldap1.url: ldap://ldap1.test.com:839" //
                , "georchestra.gateway.security.ldap.ldap1.baseDn: dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap1.users.rdn: ou=users,dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap1.users.searchFilter: (uid={0})" //
                , "georchestra.gateway.security.ldap.ldap1.roles.rdn: ou=roles" //
                , "georchestra.gateway.security.ldap.ldap1.roles.searchFilter: (member={0})" //
                , "georchestra.gateway.security.ldap.ldap1.orgs.rdn: ou=orgs" //
                //
                , "georchestra.gateway.security.ldap.ldap2.enabled: true" //
                , "georchestra.gateway.security.ldap.ldap2.extended: true" //
                , "georchestra.gateway.security.ldap.ldap2.url: ldap://ldap1.test.com:839" //
                , "georchestra.gateway.security.ldap.ldap2.baseDn: dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap2.users.rdn: ou=users,dc=georchestra,dc=org" //
                , "georchestra.gateway.security.ldap.ldap2.users.searchFilter: (uid={0})" //
                , "georchestra.gateway.security.ldap.ldap2.roles.rdn: ou=roles" //
                , "georchestra.gateway.security.ldap.ldap2.roles.searchFilter: (member={0})" //
                , "georchestra.gateway.security.ldap.ldap2.orgs.rdn: ou=orgs" //
        ).run(context -> {

            assertThat(context).hasNotFailed();
            assertThat(context.getBean("enabledExtendedLdapConfigs", List.class)).hasSize(2);
            assertThat(context.getBean("extendedLdapAuthenticationProviders", List.class)).hasSize(2);
            assertThat(context.getBean("extendedLdapAuthenticationProviders", List.class)).hasSize(2)
                    .allMatch(GeorchestraLdapAuthenticationProvider.class::isInstance);

            assertThat(context.getBean("georchestraLdapAuthenticatedUserMapper"))
                    .isInstanceOf(GeorchestraLdapAuthenticatedUserMapper.class);

            assertThat(context.getBean(DemultiplexingUsersApi.class)).hasFieldOrPropertyWithValue("targetNames",
                    Set.of("ldap1", "ldap2"));
        });
    }
}
