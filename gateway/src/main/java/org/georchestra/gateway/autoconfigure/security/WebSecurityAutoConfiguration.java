/*
 * Copyright (C) 2021 by the geOrchestra PSC
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

import org.georchestra.gateway.security.GatewaySecurityConfiguration;
import org.georchestra.gateway.security.accessrules.AccessRulesConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for web security in geOrchestra Gateway.
 * <p>
 * This configuration is applied only when Spring Security's default web
 * security is enabled, as determined by
 * {@link ConditionalOnDefaultWebSecurity}.
 * </p>
 *
 * <p>
 * It imports:
 * <ul>
 * <li>{@link GatewaySecurityConfiguration} - Configures security settings for
 * the gateway.</li>
 * <li>{@link AccessRulesConfiguration} - Manages access rules and security
 * policies.</li>
 * </ul>
 * </p>
 *
 * @see GatewaySecurityConfiguration
 * @see AccessRulesConfiguration
 * @see ConditionalOnDefaultWebSecurity
 */
@AutoConfiguration
@ConditionalOnDefaultWebSecurity
@Import({ GatewaySecurityConfiguration.class, AccessRulesConfiguration.class })
public class WebSecurityAutoConfiguration {
}
