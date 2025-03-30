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
package org.georchestra.gateway.autoconfigure.logging.mdc;

import java.util.Optional;

import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.SpringEnvironmentMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.webflux.MDCWebFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for MDC (Mapped Diagnostic Context) in WebFlux
 * applications.
 * <p>
 * This configuration automatically sets up the {@link MDCWebFilter} for
 * reactive web applications, enabling MDC context propagation across the
 * reactive chain. This ensures that logging statements can include contextual
 * information about the current request, such as request ID, user ID, and other
 * configured attributes.
 * <p>
 * The configuration activates only for reactive web applications (WebFlux) and
 * is controlled through the property {@code logging.mdc.enabled}.
 * <p>
 * MDC properties are controlled through various configuration properties
 * classes:
 * <ul>
 * <li>{@link HttpRequestMdcConfigProperties} - Controls which request
 * attributes are included in the MDC</li>
 * <li>{@link AuthenticationMdcConfigProperties} - Controls which user
 * authentication attributes are included</li>
 * <li>{@link SpringEnvironmentMdcConfigProperties} - Controls which application
 * environment attributes are included</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties({ HttpRequestMdcConfigProperties.class, AuthenticationMdcConfigProperties.class,
        SpringEnvironmentMdcConfigProperties.class })
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class LoggingMDCWebFluxAutoConfiguration {

    /**
     * Creates the MDCWebFilter bean for WebFlux applications.
     * <p>
     * This bean is responsible for:
     * <ol>
     * <li>Capturing HTTP request information</li>
     * <li>Adding it to the MDC</li>
     * <li>Propagating the MDC through the Reactor Context</li>
     * </ol>
     * <p>
     * The filter uses various configuration properties to determine which
     * attributes to include in the MDC:
     * <ul>
     * <li>{@link HttpRequestMdcConfigProperties} - For HTTP request attributes</li>
     * <li>{@link AuthenticationMdcConfigProperties} - For user authentication
     * attributes</li>
     * <li>{@link SpringEnvironmentMdcConfigProperties} - For application
     * environment attributes</li>
     * </ul>
     *
     * @param httpConfig      the HTTP request MDC configuration properties
     * @param authConfig      the authentication MDC configuration properties
     * @param appConfig       the application environment MDC configuration
     *                        properties
     * @param env             the Spring environment
     * @param buildProperties optional build properties for application info
     * @return the configured MDCWebFilter bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "logging.mdc.enabled", havingValue = "true", matchIfMissing = true)
    MDCWebFilter mdcWebFluxFilter(HttpRequestMdcConfigProperties httpConfig,
            AuthenticationMdcConfigProperties authConfig, SpringEnvironmentMdcConfigProperties appConfig,
            Environment env, Optional<BuildProperties> buildProperties) {
        return new MDCWebFilter(httpConfig, authConfig, appConfig, env, buildProperties);
    }
}