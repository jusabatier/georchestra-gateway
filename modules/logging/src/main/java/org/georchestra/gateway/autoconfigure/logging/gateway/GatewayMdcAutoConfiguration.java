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
package org.georchestra.gateway.autoconfigure.logging.gateway;

import java.util.Optional;

import org.georchestra.gateway.logging.accesslog.AccessLogFilterConfig;
import org.georchestra.gateway.logging.accesslog.AccessLogWebfluxFilter;
import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.SpringEnvironmentMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.webflux.MDCWebFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.NonNull;
import reactor.core.publisher.Mono;

/**
 * Auto-configuration for MDC and access logging in Spring Cloud Gateway
 * applications.
 * <p>
 * This configuration provides Gateway-specific adaptors for the MDC and access
 * logging filters, wrapping them as {@link GlobalFilter}s to integrate with the
 * Gateway filter chain. This ensures that MDC context propagation and access
 * logging work correctly in the Gateway environment, where requests flow
 * through a different filter chain than standard WebFlux applications.
 * <p>
 * The configuration activates only for reactive web applications (WebFlux) when
 * the Spring Cloud Gateway GlobalFilter class is present, and provides:
 * <ul>
 * <li>A GlobalFilter adapter for the MDCWebFilter</li>
 * <li>A GlobalFilter adapter for the AccessLogWebfluxFilter</li>
 * </ul>
 * <p>
 * Both filters can be enabled/disabled independently through configuration
 * properties.
 */
@AutoConfiguration
@EnableConfigurationProperties({ HttpRequestMdcConfigProperties.class, AuthenticationMdcConfigProperties.class,
        SpringEnvironmentMdcConfigProperties.class, AccessLogFilterConfig.class })
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(GlobalFilter.class)
public class GatewayMdcAutoConfiguration {

    /**
     * Adapts the {@link MDCWebFilter} as a {@link GlobalFilter} for Spring Cloud
     * Gateway.
     * <p>
     * This adapter allows the MDCWebFilter to be integrated into the Gateway filter
     * chain, ensuring that MDC context propagation works correctly in the Gateway
     * environment.
     * <p>
     * The filter is activated only when the property {@code logging.mdc.enabled} is
     * true (which is the default).
     */
    @Bean
    @ConditionalOnProperty(name = "logging.mdc.enabled", havingValue = "true", matchIfMissing = true)
    GlobalFilter mdcGlobalFilter(HttpRequestMdcConfigProperties httpConfig,
            AuthenticationMdcConfigProperties authConfig, SpringEnvironmentMdcConfigProperties appConfig,
            Environment env, Optional<BuildProperties> buildProperties) {
        MDCWebFilter filter = new MDCWebFilter(httpConfig, authConfig, appConfig, env, buildProperties);
        return new MdcGlobalFilterAdapter(filter);
    }

    /**
     * Adapts the {@link AccessLogWebfluxFilter} as a {@link GlobalFilter} for
     * Spring Cloud Gateway.
     * <p>
     * This adapter allows the AccessLogWebfluxFilter to be integrated into the
     * Gateway filter chain, ensuring that access logging works correctly in the
     * Gateway environment.
     * <p>
     * The filter is activated only when the property
     * {@code logging.accesslog.enabled} is true (which is the default). This
     * property is distinct from the one that controls the standard WebFlux access
     * logging filter ({@code logging.accesslog.webflux.enabled}), allowing Gateway
     * access logging to be configured independently.
     */
    @Bean
    @ConditionalOnProperty(name = AccessLogFilterConfig.ENABLED_KEY, havingValue = "true", matchIfMissing = true)
    GlobalFilter accessLogGlobalFilter(AccessLogFilterConfig conf) {
        AccessLogWebfluxFilter filter = new AccessLogWebfluxFilter(conf);
        return new AccessLogGlobalFilterAdapter(filter);
    }

    /**
     * Adapter class that wraps a {@link MDCWebFilter} and exposes it as a
     * {@link GlobalFilter}.
     * <p>
     * This adapter ensures that the MDCWebFilter is properly integrated into the
     * Gateway filter chain, allowing it to capture and propagate MDC context across
     * the Gateway's reactive processing pipeline.
     */
    static class MdcGlobalFilterAdapter implements GlobalFilter, Ordered {
        private final @NonNull MDCWebFilter delegate;

        MdcGlobalFilterAdapter(MDCWebFilter delegate) {
            this.delegate = delegate;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange,
                org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
            return delegate.filter(exchange, new WebFilterChain() {
                @Override
                public Mono<Void> filter(ServerWebExchange exchange) {
                    return chain.filter(exchange);
                }
            });
        }

        @Override
        public int getOrder() {
            return delegate.getOrder();
        }
    }

    /**
     * Adapter class that wraps an {@link AccessLogWebfluxFilter} and exposes it as
     * a {@link GlobalFilter}.
     * <p>
     * This adapter ensures that the AccessLogWebfluxFilter is properly integrated
     * into the Gateway filter chain, allowing it to capture and log access
     * information across the Gateway's reactive processing pipeline.
     */
    static class AccessLogGlobalFilterAdapter implements GlobalFilter, Ordered {
        private final @NonNull AccessLogWebfluxFilter delegate;

        AccessLogGlobalFilterAdapter(AccessLogWebfluxFilter delegate) {
            this.delegate = delegate;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange,
                org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
            return delegate.filter(exchange, new WebFilterChain() {
                @Override
                public Mono<Void> filter(ServerWebExchange exchange) {
                    return chain.filter(exchange);
                }
            });
        }

        @Override
        public int getOrder() {
            return delegate.getOrder();
        }
    }
}