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
package org.georchestra.gateway.autoconfigure.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.georchestra.gateway.autoconfigure.logging.mdc.LoggingMDCWebFluxAutoConfiguration;
import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.SpringEnvironmentMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.webflux.MDCWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

class LoggingAutoConfigurationTest {

    // Only test MDC auto-configuration
    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingMDCWebFluxAutoConfiguration.class))
            // Hide Spring Cloud Gateway classes to avoid @ConditionalOnMissingClass issues
            .withClassLoader(new FilteredClassLoader("org.springframework.cloud.gateway"));

    @Test
    void shouldRegisterMdcAutoConfigurationBeans() {
        contextRunner.withPropertyValues("logging.mdc.enabled=true").run(context -> {
            // Verify configuration properties beans are registered
            assertThat(context).hasSingleBean(HttpRequestMdcConfigProperties.class);
            assertThat(context).hasSingleBean(AuthenticationMdcConfigProperties.class);
            assertThat(context).hasSingleBean(SpringEnvironmentMdcConfigProperties.class);

            // Verify MDC filter is registered
            assertThat(context).hasSingleBean(MDCWebFilter.class);
        });
    }

    @Test
    void shouldNotRegisterMdcFilterWhenDisabled() {
        contextRunner.withPropertyValues("logging.mdc.enabled=false").run(context -> {
            // Config properties should still be registered
            assertThat(context).hasSingleBean(HttpRequestMdcConfigProperties.class);
            assertThat(context).hasSingleBean(AuthenticationMdcConfigProperties.class);
            assertThat(context).hasSingleBean(SpringEnvironmentMdcConfigProperties.class);

            // MDC filter should not be registered
            assertThat(context).doesNotHaveBean(MDCWebFilter.class);
        });
    }

    @Test
    void shouldRespectConditionalOnMissingBean() {
        contextRunner.withUserConfiguration(CustomFilterConfiguration.class).run(context -> {
            // Verify our custom beans were used instead of auto-configured ones
            assertThat(context).hasSingleBean(MDCWebFilter.class);
            assertThat(context.getBean(MDCWebFilter.class)).isExactlyInstanceOf(CustomMDCWebFilter.class);
        });
    }

    @Configuration
    static class CustomFilterConfiguration {

        @Bean
        MDCWebFilter customMdcWebFilter(HttpRequestMdcConfigProperties httpConfig,
                AuthenticationMdcConfigProperties authConfig, SpringEnvironmentMdcConfigProperties appConfig) {
            return new CustomMDCWebFilter(httpConfig, authConfig, appConfig);
        }

        @Bean
        Environment environment() {
            return mock(Environment.class);
        }
    }

    static class CustomMDCWebFilter extends MDCWebFilter {
        public CustomMDCWebFilter(HttpRequestMdcConfigProperties httpConfig,
                AuthenticationMdcConfigProperties authConfig, SpringEnvironmentMdcConfigProperties appConfig) {
            super(httpConfig, authConfig, appConfig, mock(Environment.class), Optional.empty());
        }
    }
}