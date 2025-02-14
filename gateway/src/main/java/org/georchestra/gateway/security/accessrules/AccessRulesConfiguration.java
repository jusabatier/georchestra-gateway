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
package org.georchestra.gateway.security.accessrules;

import org.georchestra.gateway.model.GatewayConfigProperties;
import org.georchestra.gateway.security.GeorchestraUserMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures geOrchestra-specific access rules based on role-based security
 * policies.
 * <p>
 * This configuration registers the {@link AccessRulesCustomizer}, which applies
 * role-based access rules to incoming requests based on the settings defined in
 * {@link GatewayConfigProperties}.
 * </p>
 * 
 * <p>
 * The rules are configured globally and can be overridden on a per-service
 * basis via {@code georchestra.gateway.services.[service].access-rules}.
 * </p>
 * 
 * @see AccessRulesCustomizer
 * @see GatewayConfigProperties#getGlobalAccessRules()
 * @see GatewayConfigProperties#getServices()
 */
@Configuration
@EnableConfigurationProperties(GatewayConfigProperties.class)
public class AccessRulesConfiguration {

    /**
     * Registers the {@link AccessRulesCustomizer} bean to enforce role-based access
     * rules.
     *
     * @param config     the gateway configuration properties
     * @param userMapper the user identity resolver for extracting user roles
     * @return an instance of {@link AccessRulesCustomizer}
     */
    @Bean
    AccessRulesCustomizer georchestraAccessRulesCustomizer(GatewayConfigProperties config,
            GeorchestraUserMapper userMapper) {
        return new AccessRulesCustomizer(config, userMapper);
    }
}
