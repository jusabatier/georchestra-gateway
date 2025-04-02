/*
 * Copyright (C) 2025 by the geOrchestra PSC
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

import org.georchestra.gateway.logging.accesslog.AccessLogFilterConfig;
import org.georchestra.gateway.logging.accesslog.AccessLogWebfluxFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for access logging in WebFlux applications.
 * <p>
 * This configuration automatically sets up the {@link AccessLogWebfluxFilter}
 * for reactive web applications, enabling HTTP request access logging. The
 * filter captures key information about each request and logs it at the
 * appropriate level based on the configuration.
 * <p>
 * The configuration activates only for reactive web applications (WebFlux) and
 * provides:
 * <ul>
 * <li>Configuration properties for controlling which requests are logged and at
 * what level</li>
 * <li>The AccessLogWebfluxFilter that performs the actual logging</li>
 * </ul>
 * <p>
 * Access log properties are controlled through the
 * {@link AccessLogFilterConfig} class, which allows defining patterns for
 * requests to be logged at different levels (info, debug, trace).
 * <p>
 * In Spring Cloud Gateway applications, this filter is not activated by default
 * to avoid double-logging, as Gateway uses its own filter chain with a
 * dedicated access log filter. This behavior can be overridden with the
 * property {@code logging.accesslog.webflux.enabled}.
 */
@AutoConfiguration
@EnableConfigurationProperties(AccessLogFilterConfig.class)
@ConditionalOnWebApplication(type = Type.REACTIVE)
// Don't activate in Gateway applications by default to avoid double logging
// The Gateway-specific filter will be created by GatewayMdcAutoConfiguration instead
@ConditionalOnMissingClass("org.springframework.cloud.gateway.filter.GlobalFilter")
// Unless explicitly enabled with this property
@ConditionalOnProperty(name = "logging.accesslog.webflux.enabled", havingValue = "true", matchIfMissing = true)
public class AccessLogWebFluxAutoConfiguration {

    /**
     * Creates the AccessLogWebfluxFilter bean for WebFlux applications.
     * <p>
     * This bean is responsible for logging HTTP requests based on the provided
     * configuration. The filter captures key information about each request and
     * logs it at the appropriate level based on the URL patterns defined in the
     * configuration.
     * <p>
     * The filter is configured with the {@link AccessLogFilterConfig} which
     * determines:
     * <ul>
     * <li>Which URL patterns are logged</li>
     * <li>What log level (info, debug, trace) is used for each pattern</li>
     * </ul>
     * <p>
     * In Spring Cloud Gateway applications, this bean is not created by default to
     * avoid double-logging with the Gateway's GlobalFilter. The Gateway
     * configuration creates its own dedicated instance of AccessLogWebfluxFilter
     * wrapped in a GlobalFilter adapter.
     *
     * @param conf the access log filter configuration properties
     * @return the configured AccessLogWebfluxFilter bean
     */
    @Bean
    AccessLogWebfluxFilter accessLogFilter(AccessLogFilterConfig conf) {
        return new AccessLogWebfluxFilter(conf);
    }
}