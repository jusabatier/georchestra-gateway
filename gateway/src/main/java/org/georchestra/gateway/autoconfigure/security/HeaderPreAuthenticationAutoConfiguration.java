/*
 * Copyright (C) 2023 by the geOrchestra PSC
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
package org.georchestra.gateway.autoconfigure.security;

import org.georchestra.gateway.security.preauth.HeaderPreAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for request headers pre-authentication.
 * <p>
 * This configuration enables header-based pre-authentication when the
 * {@code georchestra.gateway.security.header-authentication.enabled} property
 * is set to {@code true}.
 * </p>
 *
 * <p>
 * It imports {@link HeaderPreAuthenticationConfiguration}, which provides the
 * necessary beans for handling pre-authentication via request headers.
 * </p>
 *
 * @see ConditionalOnHeaderPreAuthentication
 * @see HeaderPreAuthenticationConfiguration
 */
@AutoConfiguration
@ConditionalOnHeaderPreAuthentication
@Import(HeaderPreAuthenticationConfiguration.class)
public class HeaderPreAuthenticationAutoConfiguration {
}
