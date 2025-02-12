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

import org.georchestra.gateway.accounts.admin.AccountCreated;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Configures RabbitMQ event handling for geOrchestra account creation events.
 * <p>
 * This configuration enables the system to send events via RabbitMQ when an
 * account is created in geOrchestra's LDAP in response to pre-authenticated or
 * OpenID Connect (OIDC) authentication.
 * </p>
 * <p>
 * When an {@link AccountCreated} event is published, it is intercepted and
 * forwarded to the RabbitMQ event queue.
 * </p>
 *
 * <p>
 * This configuration also imports RabbitMQ-related XML context files:
 * <ul>
 * <li>{@code rabbit-listener-context.xml} - Configures message listeners</li>
 * <li>{@code rabbit-sender-context.xml} - Configures message senders</li>
 * </ul>
 * </p>
 *
 * @see AccountCreated
 * @see RabbitmqEventsConfigurationProperties
 */
@Configuration
@EnableConfigurationProperties(RabbitmqEventsConfigurationProperties.class)
@ImportResource({ "classpath:rabbit-listener-context.xml", "classpath:rabbit-sender-context.xml" })
public class RabbitmqEventsConfiguration {

    /**
     * Defines the RabbitMQ event sender for publishing account creation events.
     *
     * @param eventTemplate the RabbitMQ {@link RabbitTemplate} used for message
     *                      publishing
     * @return an instance of {@link RabbitmqAccountCreatedEventSender}
     */
    @Bean
    RabbitmqAccountCreatedEventSender eventsSender(@Qualifier("eventTemplate") RabbitTemplate eventTemplate) {
        return new RabbitmqAccountCreatedEventSender(eventTemplate);
    }

    /**
     * Configures a RabbitMQ connection factory.
     * <p>
     * This method initializes a {@link CachingConnectionFactory} using the RabbitMQ
     * connection properties defined in
     * {@link RabbitmqEventsConfigurationProperties}.
     * </p>
     *
     * @param config the RabbitMQ configuration properties
     * @return a configured {@link CachingConnectionFactory} instance
     */
    @Bean
    CachingConnectionFactory connectionFactory(RabbitmqEventsConfigurationProperties config) {
        com.rabbitmq.client.ConnectionFactory fac = new com.rabbitmq.client.ConnectionFactory();
        fac.setHost(config.getHost());
        fac.setPort(config.getPort());
        fac.setUsername(config.getUser());
        fac.setPassword(config.getPassword());

        return new CachingConnectionFactory(fac);
    }

    /**
     * Configures a health indicator for monitoring the RabbitMQ connection status.
     * <p>
     * This health indicator integrates with Spring Boot Actuator, allowing
     * real-time monitoring of the RabbitMQ connection through health endpoints.
     * </p>
     *
     * @param eventTemplate the RabbitMQ template used for event communication
     * @return a configured {@link RabbitHealthIndicator}
     */
    @Bean
    RabbitHealthIndicator rabbitHealthIndicator(@Qualifier("eventTemplate") RabbitTemplate eventTemplate) {
        return new RabbitHealthIndicator(eventTemplate);
    }
}
