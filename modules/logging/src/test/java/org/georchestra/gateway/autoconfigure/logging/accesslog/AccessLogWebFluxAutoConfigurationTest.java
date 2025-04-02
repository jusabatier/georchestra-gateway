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
package org.georchestra.gateway.autoconfigure.logging.accesslog;

import static org.assertj.core.api.Assertions.assertThat;

import org.georchestra.gateway.logging.accesslog.AccessLogFilterConfig;
import org.georchestra.gateway.logging.accesslog.AccessLogWebfluxFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.cloud.gateway.filter.GlobalFilter;

/**
 * Tests for the AccessLogWebFluxAutoConfiguration class.
 */
class AccessLogWebFluxAutoConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AccessLogWebFluxAutoConfiguration.class))
            // Exclude Spring Cloud Gateway to avoid @ConditionalOnMissingClass issues
            .withClassLoader(new FilteredClassLoader(GlobalFilter.class));

    @Test
    void shouldRegisterAccessLogBeans() {
        contextRunner.withPropertyValues("logging.accesslog.webflux.enabled=true").run(context -> {
            // Verify configuration properties beans are registered
            assertThat(context).hasSingleBean(AccessLogFilterConfig.class);

            // Verify access log filter is registered
            assertThat(context).hasSingleBean(AccessLogWebfluxFilter.class);
        });
    }

    @Test
    void shouldNotRegisterAccessLogBeansWhenDisabled() {
        contextRunner.withPropertyValues("logging.accesslog.webflux.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(AccessLogFilterConfig.class);

            // Filter should not be registered
            assertThat(context).doesNotHaveBean(AccessLogWebfluxFilter.class);
        });
    }
}