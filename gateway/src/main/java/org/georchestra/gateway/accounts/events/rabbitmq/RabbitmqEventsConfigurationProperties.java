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
package org.georchestra.gateway.accounts.events.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.Generated;

/**
 * Configuration properties to enable rabbit-mq event dispatching of accounts
 * created
 */
@Data
@Generated
@Validated
@ConfigurationProperties(prefix = RabbitmqEventsConfigurationProperties.PREFIX)
public class RabbitmqEventsConfigurationProperties {

    public static final String PREFIX = "georchestra.gateway.security.events.rabbitmq";
    public static final String ENABLED = PREFIX + ".enabled";

    /**
     * Whether rabbit-mq events should be sent when an LDAP account was created upon
     * a first successful login through OAuth2
     */
    private boolean enabled;
    /**
     * The rabbit-mq host name
     */
    private String host;
    /**
     * The rabbit-mq host port number
     */
    private int port;
    /**
     * The rabbit-mq authentication user
     */
    private String user;
    /**
     * The rabbit-mq authentication password
     */
    private String password;
}
