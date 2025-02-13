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
 * {@link Configuration @Configuration} to enable sending events over rabbitmq *
 * <p>
 * When an account is created in geOrchestra's LDAP in response to a
 * pre-authenticated or OIDC successful authentication, an
 * {@link AccountCreated} event will be catch up and sent over the wire.
 * 
 * @see RabbitmqEventsConfigurationProperties
 * 
 */
@Configuration
@EnableConfigurationProperties(RabbitmqEventsConfigurationProperties.class)
@ImportResource({ "classpath:rabbit-listener-context.xml", "classpath:rabbit-sender-context.xml" })
public class RabbitmqEventsConfiguration {

    @Bean
    RabbitmqAccountCreatedEventSender eventsSender(@Qualifier("eventTemplate") RabbitTemplate eventTemplate) {
        return new RabbitmqAccountCreatedEventSender(eventTemplate);
    }

    @Bean
    org.springframework.amqp.rabbit.connection.CachingConnectionFactory connectionFactory(
            RabbitmqEventsConfigurationProperties config) {

        com.rabbitmq.client.ConnectionFactory fac = new com.rabbitmq.client.ConnectionFactory();
        fac.setHost(config.getHost());
        fac.setPort(config.getPort());
        fac.setUsername(config.getUser());
        fac.setPassword(config.getPassword());

        return new CachingConnectionFactory(fac);
    }

    @Bean
    RabbitHealthIndicator rabbitHealthIndicator(@Qualifier("eventTemplate") RabbitTemplate eventTemplate) {
        return new RabbitHealthIndicator(eventTemplate);
    }

}
