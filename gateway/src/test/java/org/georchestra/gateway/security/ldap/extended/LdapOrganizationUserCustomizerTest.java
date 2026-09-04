/*
 * Copyright (C) 2026 by the geOrchestra PSC
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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.security.ldap.extended;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

class LdapOrganizationUserCustomizerTest {

    private final DemultiplexingUsersApi users = mock(DemultiplexingUsersApi.class);

    private final LdapOrganizationUserCustomizer customizer = new LdapOrganizationUserCustomizer(users);

    @Test
    void ignoresNonOAuth2Authentication() {
        GeorchestraUser mappedUser = new GeorchestraUser();

        GeorchestraUser result = customizer.apply(mock(Authentication.class), mappedUser);

        assertThat(result).isSameAs(mappedUser);
        verifyNoInteractions(users);
    }

    @Test
    void enrichesExistingUserFoundByUsername() {
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GeorchestraUser mappedUser = oauth2User();
        Organization organization = mock(Organization.class);
        ExtendedGeorchestraUser ldapUser = new ExtendedGeorchestraUser(new GeorchestraUser()).setOrg(organization);
        when(users.findByUsername("jdoe")).thenReturn(Optional.of(ldapUser));

        GeorchestraUser result = customizer.apply(authentication, mappedUser);

        assertThat(result).isInstanceOf(ExtendedGeorchestraUser.class).isNotSameAs(ldapUser);
        assertThat(((ExtendedGeorchestraUser) result).getOrg()).isSameAs(organization);
        assertThat(result.getUsername()).isEqualTo("jdoe");
        assertThat(result.getOrganization()).isEqualTo("TEST_ORG");
        verify(users).findByUsername("jdoe");
    }

    @Test
    void keepsMappedUserWhenNoExistingLdapAccountIsFound() {
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GeorchestraUser mappedUser = oauth2User();
        when(users.findByUsername("jdoe")).thenReturn(Optional.empty());

        GeorchestraUser result = customizer.apply(authentication, mappedUser);

        assertThat(result).isSameAs(mappedUser);
    }

    @Test
    void keepsMappedUserWhenExistingLdapAccountHasNoOrganization() {
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GeorchestraUser mappedUser = oauth2User();
        ExtendedGeorchestraUser ldapUser = new ExtendedGeorchestraUser(new GeorchestraUser());
        when(users.findByUsername("jdoe")).thenReturn(Optional.of(ldapUser));

        GeorchestraUser result = customizer.apply(authentication, mappedUser);

        assertThat(result).isSameAs(mappedUser);
    }

    private GeorchestraUser oauth2User() {
        GeorchestraUser user = new GeorchestraUser();
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        user.setOrganization("TEST_ORG");
        user.setOAuth2Provider("keycloak-oauth2");
        user.setOAuth2Uid("jdoe");
        return user;
    }
}
