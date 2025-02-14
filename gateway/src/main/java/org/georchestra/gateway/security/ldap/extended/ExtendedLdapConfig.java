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

import java.util.Optional;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Generated;
import lombok.NonNull;
import lombok.Value;

/**
 * Configuration properties for an extended LDAP authentication source.
 * <p>
 * This class represents the settings required to connect and authenticate
 * against an extended geOrchestra LDAP directory, including user and role
 * search configurations, as well as optional administrator credentials.
 * </p>
 *
 * <p>
 * Extended LDAP configurations include additional organization-related fields
 * used for enhanced user management.
 * </p>
 */
@Value
@Builder
@Generated
public class ExtendedLdapConfig {

    /**
     * The unique identifier for this LDAP configuration.
     */
    private @NonNull String name;

    /**
     * Flag indicating whether this LDAP configuration is enabled.
     */
    private boolean enabled;

    /**
     * The LDAP server URL.
     */
    private @NonNull String url;

    /**
     * The base distinguished name (DN) of the LDAP directory.
     */
    private @NonNull String baseDn;

    /**
     * The relative distinguished name (RDN) of the user entries.
     */
    private @NonNull String usersRdn;

    /**
     * The search filter used to find user entries in the LDAP directory.
     */
    private @NonNull String usersSearchFilter;

    /**
     * The relative distinguished name (RDN) of the role entries.
     */
    private @NonNull String rolesRdn;

    /**
     * The search filter used to find role entries in the LDAP directory.
     */
    private @NonNull String rolesSearchFilter;

    /**
     * The attributes to be retrieved for users.
     * <p>
     * A {@code null} value indicates all attributes should be returned, while an
     * empty array means no attributes will be returned.
     * </p>
     */
    private String[] returningAttributes;

    /**
     * Optional administrator distinguished name (DN) for performing privileged
     * operations.
     */
    @Default
    private @NonNull Optional<String> adminDn = Optional.empty();

    /**
     * Optional administrator password for privileged operations.
     */
    @Default
    private @NonNull Optional<String> adminPassword = Optional.empty();

    /**
     * The relative distinguished name (RDN) of the organization entries.
     */
    private @NonNull String orgsRdn;

    /**
     * The relative distinguished name (RDN) of the pending organization entries.
     */
    private @NonNull String pendingOrgsRdn;
}
