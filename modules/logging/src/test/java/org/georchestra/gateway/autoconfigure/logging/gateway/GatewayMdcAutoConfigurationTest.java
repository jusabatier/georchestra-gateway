/*
 * Copyright (C) 2024 by the geOrchestra PSC
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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.autoconfigure.logging.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.georchestra.gateway.autoconfigure.logging.accesslog.AccessLogWebFluxAutoConfiguration;
import org.georchestra.gateway.autoconfigure.logging.mdc.LoggingMDCWebFluxAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.cloud.gateway.filter.GlobalFilter;

/**
 * Tests for the GatewayMdcAutoConfiguration class using Spring Boot's
 * auto-configuration testing facilities.
 */
class GatewayMdcAutoConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AccessLogWebFluxAutoConfiguration.class,
                    LoggingMDCWebFluxAutoConfiguration.class, GatewayMdcAutoConfiguration.class));

    @Test
    void shouldRegisterMdcGlobalFilterByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GatewayMdcAutoConfiguration.class);

            // Verify that the MDC GlobalFilter is registered
            assertThat(context.getBeansOfType(GlobalFilter.class).values().stream()
                    .anyMatch(filter -> filter instanceof GatewayMdcAutoConfiguration.MdcGlobalFilterAdapter)).isTrue();

            // Check total number of GlobalFilters
            assertThat(context.getBeansOfType(GlobalFilter.class)).hasSize(2);
        });
    }

    @Test
    void shouldNotRegisterMdcGlobalFilterWhenDisabled() {
        contextRunner.withPropertyValues("logging.mdc.enabled=false").run(context -> {
            assertThat(context).hasSingleBean(GatewayMdcAutoConfiguration.class);

            // Verify that no MDC GlobalFilter is registered
            assertThat(context.getBeansOfType(GlobalFilter.class).values().stream()
                    .noneMatch(filter -> filter instanceof GatewayMdcAutoConfiguration.MdcGlobalFilterAdapter))
                            .isTrue();
        });
    }

    @Test
    void shouldRegisterAccessLogGlobalFilterByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GatewayMdcAutoConfiguration.class);

            // Verify that the AccessLog GlobalFilter is registered
            assertThat(context.getBeansOfType(GlobalFilter.class).values().stream()
                    .anyMatch(filter -> filter instanceof GatewayMdcAutoConfiguration.AccessLogGlobalFilterAdapter))
                            .isTrue();
        });
    }

    @Test
    void shouldNotRegisterAccessLogGlobalFilterWhenDisabled() {
        contextRunner.withPropertyValues("logging.accesslog.enabled=false").run(context -> {
            assertThat(context).hasSingleBean(GatewayMdcAutoConfiguration.class);

            // Verify that no AccessLog GlobalFilter is registered
            assertThat(context.getBeansOfType(GlobalFilter.class).values().stream()
                    .noneMatch(filter -> filter instanceof GatewayMdcAutoConfiguration.AccessLogGlobalFilterAdapter))
                            .isTrue();
        });
    }

    @Test
    void shouldRespectBothConfigurationProperties() {
        contextRunner.withPropertyValues("logging.mdc.enabled=false", "logging.accesslog.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(GatewayMdcAutoConfiguration.class);

                    // Verify that no GlobalFilters of either type are registered
                    assertThat(context.getBeansOfType(GlobalFilter.class)).isEmpty();
                });
    }
}