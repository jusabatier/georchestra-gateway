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
package org.georchestra.gateway.autoconfigure.accounts;

import org.georchestra.gateway.accounts.admin.AccountCreated;
import org.georchestra.gateway.accounts.events.rabbitmq.RabbitmqEventsConfiguration;
import org.georchestra.gateway.accounts.events.rabbitmq.RabbitmqEventsConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for enabling RabbitMQ event dispatching when configured.
 * <p>
 * This configuration enables RabbitMQ integration when the following conditions
 * are met:
 * <ul>
 * <li>LDAP account creation is enabled
 * ({@link ConditionalOnCreateLdapAccounts}).</li>
 * <li>The property {@code georchestra.gateway.security.events.rabbitmq} is set
 * to {@code true}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * When a user account is created in geOrchestra's LDAP following a successful
 * authentication via pre-authenticated headers or OIDC, an
 * {@link AccountCreated} event is published. This event is then transmitted via
 * RabbitMQ.
 * </p>
 *
 * <p>
 * This class imports {@link RabbitmqEventsConfiguration}, which defines the
 * RabbitMQ message sender and event handling beans.
 * </p>
 *
 * @see ConditionalOnCreateLdapAccounts
 * @see RabbitmqEventsConfiguration
 * @see RabbitmqEventsConfigurationProperties
 */
@AutoConfiguration
@ConditionalOnCreateLdapAccounts
@ConditionalOnProperty(name = RabbitmqEventsConfigurationProperties.ENABLED, havingValue = "true", matchIfMissing = false)
@Import(RabbitmqEventsConfiguration.class)
public class RabbitmqEventsAutoConfiguration {

}
