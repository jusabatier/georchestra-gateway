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

import java.util.Optional;

import lombok.Builder;
import lombok.Generated;
import lombok.NonNull;
import lombok.Value;

/**
 * Configuration object representing the LDAP server settings used for user
 * authentication and role retrieval.
 * <p>
 * This class defines essential connection parameters and search configurations
 * for LDAP authentication.
 * </p>
 *
 * <p>
 * Features:
 * <ul>
 * <li>Supports both standard LDAP and Active Directory.</li>
 * <li>Allows specifying search filters and base DNs for users and roles.</li>
 * <li>Supports optional admin credentials for LDAP queries.</li>
 * <li>Provides a default Active Directory search filter.</li>
 * </ul>
 * </p>
 *
 * Example usage:
 * 
 * <pre>
 * {
 *     &#64;code
 *     LdapServerConfig config = LdapServerConfig.builder().name("default").enabled(true).activeDirectory(false)
 *             .url("ldap://example.com").baseDn("dc=example,dc=com").usersRdn("ou=users")
 *             .usersSearchFilter("(uid={0})").rolesRdn("ou=roles").rolesSearchFilter("(member={0})")
 *             .adminDn(Optional.of("cn=admin,dc=example,dc=com")).adminPassword(Optional.of("secret")).build();
 * }
 * </pre>
 */
@Value
@Builder
@Generated
public class LdapServerConfig {

    /**
     * Default search filter for Active Directory user lookup.
     */
    public static final String DEFAULT_ACTIVE_DIRECTORY_USER_SEARCH_FILTER = "(&(objectClass=user)(userPrincipalName={0}))";

    /**
     * Logical name for identifying the LDAP configuration.
     */
    private @NonNull String name;

    /**
     * Flag indicating if this LDAP configuration is enabled.
     */
    private boolean enabled;

    /**
     * Indicates whether the LDAP server is an Active Directory instance.
     */
    private boolean activeDirectory;

    /**
     * LDAP server URL, including protocol and port (e.g.,
     * "ldap://ldap.example.com:389").
     */
    private @NonNull String url;

    /**
     * Base Distinguished Name (DN) for the LDAP directory.
     * <p>
     * This is the root DN where searches for users and roles begin. Example:
     * {@code dc=example,dc=com}.
     * </p>
     */
    private @NonNull String baseDn;

    /**
     * Relative Distinguished Name (RDN) for user entries within the directory.
     * <p>
     * Example: {@code ou=users}.
     * </p>
     */
    private @NonNull String usersRdn;

    /**
     * LDAP search filter for locating user entries.
     * <p>
     * Example:
     * <ul>
     * <li>OpenLDAP: {@code (uid={0})}</li>
     * <li>Active Directory:
     * {@code (&(objectClass=user)(userPrincipalName={0}))}</li>
     * </ul>
     * </p>
     */
    private @NonNull String usersSearchFilter;

    /**
     * Relative Distinguished Name (RDN) for role entries within the directory.
     * <p>
     * Example: {@code ou=roles}.
     * </p>
     */
    private @NonNull String rolesRdn;

    /**
     * LDAP search filter for retrieving user roles.
     * <p>
     * Example: {@code (member={0})}.
     * </p>
     */
    private @NonNull String rolesSearchFilter;

    /**
     * Attributes to retrieve when searching for user details.
     * <p>
     * A {@code null} value means all attributes are retrieved, while an empty array
     * means none are returned.
     * </p>
     */
    private String[] returningAttributes;

    /**
     * Optional distinguished name (DN) for an LDAP administrator account used for
     * privileged queries.
     */
    private @NonNull Optional<String> adminDn;

    /**
     * Optional password for the administrator account used for privileged queries.
     */
    private @NonNull Optional<String> adminPassword;
}
